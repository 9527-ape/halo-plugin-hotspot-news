package run.hotspotnews.service;

import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ReactiveExtensionClient;
import run.hotspotnews.dto.NewsFeedItem;
import run.hotspotnews.model.HotspotNewsItem;

/**
 * 热点新闻查询服务：为前端提供聚合后的新闻列表。
 */
@Service
public class NewsQueryService {

    private final ReactiveExtensionClient client;

    public NewsQueryService(ReactiveExtensionClient client) {
        this.client = client;
    }

    /**
     * 查询新闻列表。
     *
     * @param source 来源过滤：WEIBO / XINWEN_LIANBO / all 或空，表示全部
     */
    public Mono<List<NewsFeedItem>> listItems(String source) {
        return client.list(HotspotNewsItem.class,
                item -> matches(item, source),
                Comparator.comparing(
                    item -> item.getMetadata().getCreationTimestamp()))
            .map(NewsFeedItem::from)
            .collectList();
    }

    private boolean matches(HotspotNewsItem item, String source) {
        if (source == null || source.isBlank() || "all".equalsIgnoreCase(source)) {
            return true;
        }
        String specSource = item.getSpec().getSource();
        String expected = switch (source.toLowerCase()) {
            case "weibo" -> "WEIBO";
            case "lianbo" -> "XINWEN_LIANBO";
            default -> source.toUpperCase();
        };
        return expected.equalsIgnoreCase(specSource);
    }
}