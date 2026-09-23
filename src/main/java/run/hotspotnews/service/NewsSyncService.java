package run.hotspotnews.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.plugin.ReactiveSettingFetcher;
import run.hotspotnews.config.PluginSetting;
import run.hotspotnews.model.HotspotNewsItem;
import run.hotspotnews.service.NewsFetcher.RawNewsItem;

/**
 * 热点新闻同步服务：按配置抓取各数据源，并将结果持久化为自定义模型。
 */
@Service
public class NewsSyncService {

    private static final Logger log = LoggerFactory.getLogger(NewsSyncService.class);

    /** 写入自定义模型 labels 的来源标记 key */
    public static final String SOURCE_LABEL_KEY = "news.moyu.run/source";

    private final ReactiveExtensionClient client;
    private final ReactiveSettingFetcher settingFetcher;
    private final WebClient webClient;
    private final List<NewsFetcher> fetchers;

    private final AtomicReference<SyncStatus> statusRef =
        new AtomicReference<>(new SyncStatus("", 0, 0, 0, null));

    /** 同步互斥标志，防止定时与手动同步并发执行 */
    private final AtomicBoolean syncing = new AtomicBoolean(false);

    public NewsSyncService(ReactiveExtensionClient client,
        ReactiveSettingFetcher settingFetcher,
        WebClient webClient,
        List<NewsFetcher> fetchers) {
        this.client = client;
        this.settingFetcher = settingFetcher;
        this.webClient = webClient;
        this.fetchers = fetchers;
    }

    /** 定时任务入口：按照配置的同步间隔决定是否执行。 */
    public Mono<Void> syncIfNeeded() {
        return loadSettings().flatMap(setting -> {
            SyncStatus current = statusRef.get();
            if (current.lastSyncAt() != null && !current.lastSyncAt().isBlank()) {
                try {
                    Instant last = Instant.parse(current.lastSyncAt());
                    long elapsed = Duration.between(last, Instant.now()).toMinutes();
                    if (elapsed < setting.syncInterval()) {
                        return Mono.empty();
                    }
                } catch (Exception ignored) {
                    // 时间解析失败时直接执行一次同步
                }
            }
            return doSync(setting);
        });
    }

    /** 手动触发同步（通过自定义 API 调用）。 */
    public Mono<Void> syncNow() {
        return loadSettings().flatMap(this::doSync);
    }

    /** 读取插件配置，未配置时使用默认值。 */
    public Mono<PluginSetting> loadSettings() {
        return settingFetcher.fetch(PluginSetting.GROUP, PluginSetting.class)
            .defaultIfEmpty(PluginSetting.defaultSetting())
            .onErrorReturn(PluginSetting.defaultSetting());
    }

    public Mono<SyncStatus> currentStatus() {
        return Mono.just(statusRef.get());
    }

    private Mono<Void> doSync(PluginSetting setting) {
        // 互斥：避免定时同步与手动刷新并发执行，防止同名资源竞态
        if (!syncing.compareAndSet(false, true)) {
            return Mono.empty();
        }
        List<NewsFetcher> enabled = new ArrayList<>();
        for (NewsFetcher fetcher : fetchers) {
            if (PluginSetting.SOURCE_JOKE.equals(fetcher.source()) && setting.jokeActive()) {
                enabled.add(fetcher);
            } else if (PluginSetting.SOURCE_SOUP.equals(fetcher.source()) && setting.soupActive()) {
                enabled.add(fetcher);
            }
        }
        return Flux.fromIterable(enabled)
            .flatMap(fetcher -> fetcher.fetch(webClient, setting))
            .flatMapIterable(list -> list)
            .collectList()
            .flatMap(raws -> replaceAll(raws, setting))
            .doFinally(signal -> syncing.set(false));
    }

    /**
     * 用最新抓取结果替换旧数据：先创建新条目（跳过已存在同名），
     * 再删除不在新名单中的旧条目。
     *
     * <p>Halo 扩展客户端的索引是异步更新的，先删后建会遇到
     * “Duplicate name detected”竞态，因此采用先建后删的顺序。</p>
     */
    private Mono<Void> replaceAll(List<RawNewsItem> raws, PluginSetting setting) {
        // 每个来源限制条数
        Map<String, List<RawNewsItem>> grouped = raws.stream()
            .collect(Collectors.groupingBy(RawNewsItem::source));
        List<RawNewsItem> limited = new ArrayList<>();
        for (List<RawNewsItem> list : grouped.values()) {
            limited.addAll(list.stream().limit(setting.maxItems()).toList());
        }
        Set<String> newNames = limited.stream()
            .map(this::newsName)
            .collect(Collectors.toSet());

        return client.list(HotspotNewsItem.class, e -> true, Comparator.comparing(
                e -> e.getMetadata().getName()))
            .collectList()
            .flatMap(existing -> {
                Set<String> existingNames = existing.stream()
                    .map(e -> e.getMetadata().getName())
                    .collect(Collectors.toSet());
                return createNew(limited, existingNames)
                    .then(deleteStale(existing, newNames));
            })
            .doOnSuccess(v -> {
                int joke = count(limited, PluginSetting.SOURCE_JOKE);
                int soup = count(limited, PluginSetting.SOURCE_SOUP);
                statusRef.set(new SyncStatus(
                    Instant.now().toString(), joke, soup, limited.size(), null));
                log.info("开心内容同步完成：笑话 {} 条，鸡汤 {} 条", joke, soup);
            })
            .doOnError(e -> {
                statusRef.set(new SyncStatus(
                    statusRef.get().lastSyncAt(),
                    statusRef.get().jokeCount(),
                    statusRef.get().soupCount(),
                    statusRef.get().total(),
                    e.getMessage()
                ));
                log.warn("开心内容持久化失败：{}", e.getMessage());
            })
            .onErrorResume(e -> Mono.empty());
    }

    /** 创建新条目（已存在同名的跳过），失败的单条跳过不中断整体。 */
    private Mono<Void> createNew(List<RawNewsItem> limited, Set<String> existingNames) {
        return Flux.fromIterable(limited)
            .filter(raw -> !existingNames.contains(newsName(raw)))
            .map(this::toExtension)
            .flatMap(item -> attempt(client.create(item)))
            .then();
    }

    /** 删除不在新名单中的旧条目，失败的单条跳过不中断整体。 */
    private Mono<Void> deleteStale(List<HotspotNewsItem> existing, Set<String> newNames) {
        return Flux.fromIterable(existing)
            .filter(item -> !newNames.contains(item.getMetadata().getName()))
            .flatMap(item -> attempt(client.delete(item)))
            .then();
    }

    private <E extends run.halo.app.extension.Extension> Mono<Void> attempt(Mono<E> op) {
        return op.then()
            .onErrorResume(e -> {
                log.warn("同步操作失败（已跳过）：{}", e.getMessage());
                return Mono.empty();
            });
    }

    private static int count(List<RawNewsItem> items, String source) {
        return (int) items.stream().filter(r -> source.equals(r.source())).count();
    }

    private HotspotNewsItem toExtension(RawNewsItem raw) {
        var item = new HotspotNewsItem();
        var metadata = new Metadata();
        metadata.setName(newsName(raw));
        Map<String, String> labels = new HashMap<>();
        labels.put(SOURCE_LABEL_KEY, raw.source());
        metadata.setLabels(labels);
        item.setMetadata(metadata);

        var spec = new HotspotNewsItem.Spec();
        spec.setTitle(raw.title());
        spec.setSource(raw.source());
        spec.setUrl(raw.url());
        spec.setSummary(raw.summary());
        spec.setCategory(raw.category());
        spec.setHotValue(raw.hotValue());
        spec.setRank(raw.rank());
        spec.setPublishedDate(raw.publishedDate());
        spec.setFetchedAt(Instant.now().toString());
        item.setSpec(spec);
        return item;
    }

    /** 依据内容生成稳定的资源名称（仅小写字母、数字与 -）。 */
    private String newsName(RawNewsItem raw) {
        return "hotspot-" + sha256(
            raw.source() + "|" + raw.title() + "|" + raw.summary()).substring(0, 24);
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            return String.valueOf(input.hashCode()).replace("-", "x");
        }
    }

    /** 同步状态（内存态）。 */
    public record SyncStatus(
        String lastSyncAt,
        int jokeCount,
        int soupCount,
        int total,
        String error
    ) {
    }
}