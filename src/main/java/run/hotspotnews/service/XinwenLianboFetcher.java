package run.hotspotnews.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import run.hotspotnews.config.PluginSetting;
import run.hotspotnews.service.NewsFetcher.RawNewsItem;

/**
 * 《新闻联播》抓取器。
 *
 * <p>抓取央视网《新闻联播》栏目页（{@code https://tv.cctv.com/lm/xwlb/}），
 * 页面为服务端渲染，直接解析 HTML 中的条目列表：包含当期完整版以及每条新闻的视频条目。</p>
 */
@Component
public class XinwenLianboFetcher implements NewsFetcher {

    private static final Logger log = LoggerFactory.getLogger(XinwenLianboFetcher.class);

    /** 栏目页中的新闻列表容器 */
    private static final Pattern UL_PATTERN =
        Pattern.compile("(?s)<ul[^>]*id=\"content\"[^>]*>(.*?)</ul>");

    /** 页面中的日期，形如 2026-09-21 */
    private static final Pattern DATE_PATTERN = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})");

    /** 新闻条目链接与标题 */
    private static final Pattern LINK_PATTERN = Pattern.compile(
        "<a\\s+href=\"(https?://tv\\.cctv\\.com/\\d{4}/\\d{2}/\\d{2}/[^\"]+\\.shtml)\"[^>]*>(.*?)</a>",
        Pattern.DOTALL
    );

    private static final Pattern TAG_PATTERN = Pattern.compile("<[^>]+>");

    @Override
    public String source() {
        return PluginSetting.SOURCE_LIANBO;
    }

    @Override
    public Mono<List<RawNewsItem>> fetch(WebClient webClient, PluginSetting setting) {
        return webClient.get()
            .uri(setting.lianboPage())
            .header(HttpHeaders.USER_AGENT, setting.ua())
            .header(HttpHeaders.REFERER, "https://tv.cctv.com/")
            .retrieve()
            .bodyToMono(String.class)
            .map(this::parseItems)
            .onErrorResume(e -> {
                log.warn("抓取《新闻联播》栏目页失败：{}", e.getMessage());
                return Mono.just(List.of());
            });
    }

    private List<RawNewsItem> parseItems(String html) {
        if (html == null || html.isBlank()) {
            return List.of();
        }
        String listBlock = extractListBlock(html);
        String date = extractDate(html);

        Set<String> seenUrls = new LinkedHashSet<>();
        List<RawNewsItem> items = new ArrayList<>();
        int rank = 1;
        Matcher matcher = LINK_PATTERN.matcher(listBlock);
        while (matcher.find()) {
            String url = matcher.group(1);
            String title = TAG_PATTERN.matcher(matcher.group(2)).replaceAll("").trim();
            if (title.isEmpty() || !seenUrls.add(url)) {
                continue;
            }
            String category = title.contains("《新闻联播》") ? "完整版" : "联播条目";
            items.add(new RawNewsItem(
                source(),
                title,
                url,
                null,
                category,
                null,
                rank++,
                date
            ));
        }
        return items;
    }

    private String extractListBlock(String html) {
        Matcher matcher = UL_PATTERN.matcher(html);
        return matcher.find() ? matcher.group(1) : html;
    }

    private String extractDate(String html) {
        Matcher matcher = DATE_PATTERN.matcher(html);
        return matcher.find() ? matcher.group(1) : null;
    }
}