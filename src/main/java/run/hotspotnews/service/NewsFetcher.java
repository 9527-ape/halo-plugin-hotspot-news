package run.hotspotnews.service;

import java.util.List;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import run.hotspotnews.config.PluginSetting;

/**
 * 新闻抓取入口的统一接口。每个数据来源实现一个抓取器。
 */
public interface NewsFetcher {

    /**
     * 抓取到的原始新闻条目（尚未转换为持久化模型）。
     */
    record RawNewsItem(
        String source,
        String title,
        String url,
        String summary,
        String category,
        Integer hotValue,
        Integer rank,
        String publishedDate
    ) {
    }

    /** 来源标识，见 {@link PluginSetting#SOURCE_WEIBO} 与 {@link PluginSetting#SOURCE_LIANBO}。 */
    String source();

    /** 抓取指定来源的新闻列表。抓取失败时返回空列表，由调用方记录日志。 */
    Mono<List<RawNewsItem>> fetch(WebClient webClient, PluginSetting setting);
}