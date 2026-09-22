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
        List<NewsFetcher> enabled = new ArrayList<>();
        for (NewsFetcher fetcher : fetchers) {
            if (PluginSetting.SOURCE_WEIBO.equals(fetcher.source()) && setting.weiboActive()) {
                enabled.add(fetcher);
            } else if (PluginSetting.SOURCE_LIANBO.equals(fetcher.source()) && setting.lianboActive()) {
                enabled.add(fetcher);
            }
        }
        return Flux.fromIterable(enabled)
            .flatMap(fetcher -> fetcher.fetch(webClient, setting))
            .flatMapIterable(list -> list)
            .collectList()
            .flatMap(raws -> replaceAll(raws, setting));
    }

    /**
     * 用最新抓取结果替换全部旧数据（先删后建，保证幂等）。
     */
    private Mono<Void> replaceAll(List<RawNewsItem> raws, PluginSetting setting) {
        // 每个来源限制条数
        Map<String, List<RawNewsItem>> grouped = raws.stream()
            .collect(Collectors.groupingBy(RawNewsItem::source));
        List<RawNewsItem> limited = new ArrayList<>();
        for (List<RawNewsItem> list : grouped.values()) {
            limited.addAll(list.stream().limit(setting.maxItems()).toList());
        }

        return client.list(HotspotNewsItem.class, e -> true, Comparator.comparing(
                e -> e.getMetadata().getName()))
            .collectList()
            .flatMapMany(Flux::fromIterable)
            .flatMap(client::delete)
            .thenMany(Flux.fromIterable(limited).map(this::toExtension))
            .flatMap(client::create)
            .then()
            .doOnSuccess(v -> {
                int weibo = count(limited, PluginSetting.SOURCE_WEIBO);
                int lianbo = count(limited, PluginSetting.SOURCE_LIANBO);
                statusRef.set(new SyncStatus(
                    Instant.now().toString(), weibo, lianbo, limited.size(), null));
                log.info("热点新闻同步完成：微博 {} 条，新闻联播 {} 条", weibo, lianbo);
            })
            .doOnError(e -> {
                statusRef.set(new SyncStatus(
                    statusRef.get().lastSyncAt(),
                    statusRef.get().weiboCount(),
                    statusRef.get().lianboCount(),
                    statusRef.get().total(),
                    e.getMessage()
                ));
                log.warn("热点新闻持久化失败：{}", e.getMessage());
            })
            .onErrorResume(e -> Mono.empty());
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
        return "hotspot-" + sha256(raw.source() + "|" + raw.title() + "|" + raw.url()).substring(0, 24);
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
        int weiboCount,
        int lianboCount,
        int total,
        String error
    ) {
    }
}