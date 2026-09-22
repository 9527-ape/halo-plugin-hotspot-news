package run.hotspotnews.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import run.hotspotnews.config.PluginSetting;
import run.hotspotnews.service.NewsFetcher.RawNewsItem;

/**
 * 微博热搜抓取器。
 *
 * <p>默认调用微博热搜榜接口 {@code https://weibo.com/ajax/statuses/hot_band}，
 * 该接口返回 {@code data.band}（热搜榜）与 {@code data.hotgov}（要闻/政务）两组数据。</p>
 */
@Component
public class WeiboNewsFetcher implements NewsFetcher {

    private static final Logger log = LoggerFactory.getLogger(WeiboNewsFetcher.class);

    @Override
    public String source() {
        return PluginSetting.SOURCE_WEIBO;
    }

    @Override
    public Mono<List<RawNewsItem>> fetch(WebClient webClient, PluginSetting setting) {
        return webClient.get()
            .uri(setting.weiboApi())
            .header(HttpHeaders.USER_AGENT, setting.ua())
            .header(HttpHeaders.REFERER, "https://weibo.com/")
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToMono(WeiboHotResponse.class)
            .map(resp -> toItems(resp, setting))
            .onErrorResume(e -> {
                log.warn("抓取微博热搜失败：{}", e.getMessage());
                return Mono.just(List.of());
            });
    }

    private List<RawNewsItem> toItems(WeiboHotResponse resp, PluginSetting setting) {
        List<RawNewsItem> items = new ArrayList<>();
        if (resp == null || resp.getData() == null) {
            return items;
        }
        int rank = 1;
        if (resp.getData().getBand() != null) {
            for (Item item : resp.getData().getBand()) {
                addItem(items, item, "微博热搜", rank++, setting);
            }
        }
        if (resp.getData().getHotgov() != null) {
            for (Item item : resp.getData().getHotgov()) {
                addItem(items, item, "要闻/政务", rank++, setting);
            }
        }
        return items;
    }

    private void addItem(List<RawNewsItem> items, Item item, String category, int rank,
        PluginSetting setting) {
        String word = item.getWord();
        if (word == null || word.isBlank()) {
            return;
        }
        int hot = parseHot(item);
        if (setting.minHot() > 0 && hot < setting.minHot()) {
            return;
        }
        items.add(new RawNewsItem(
            source(),
            word.trim(),
            resolveUrl(item),
            item.getNote(),
            category,
            hot,
            rank,
            null
        ));
    }

    private String resolveUrl(Item item) {
        if (item.getWord_scheme() != null && !item.getWord_scheme().isBlank()) {
            return item.getWord_scheme();
        }
        return "https://s.weibo.com/weibo?q=%23"
            + URLEncoder.encode(item.getWord(), StandardCharsets.UTF_8) + "%23";
    }

    private int parseHot(Item item) {
        if (item.getRaw_hot() != null) {
            try {
                return Integer.parseInt(item.getRaw_hot());
            } catch (NumberFormatException ignored) {
                // fall through
            }
        }
        if (item.getNum() != null) {
            try {
                return Integer.parseInt(item.getNum());
            } catch (NumberFormatException ignored) {
                // fall through
            }
        }
        return 0;
    }

    /** 微博热搜接口响应结构（仅保留用到的字段）。 */
    public static class WeiboHotResponse {
        private Data data;

        public Data getData() {
            return data;
        }

        public void setData(Data data) {
            this.data = data;
        }
    }

    public static class Data {
        private List<Item> band;
        private List<Item> hotgov;

        public List<Item> getBand() {
            return band;
        }

        public void setBand(List<Item> band) {
            this.band = band;
        }

        public List<Item> getHotgov() {
            return hotgov;
        }

        public void setHotgov(List<Item> hotgov) {
            this.hotgov = hotgov;
        }
    }

    public static class Item {
        private String word;
        private String word_scheme;
        private String raw_hot;
        private String num;
        private String note;
        private String category;

        public String getWord() {
            return word;
        }

        public void setWord(String word) {
            this.word = word;
        }

        public String getWord_scheme() {
            return word_scheme;
        }

        public void setWord_scheme(String word_scheme) {
            this.word_scheme = word_scheme;
        }

        public String getRaw_hot() {
            return raw_hot;
        }

        public void setRaw_hot(String raw_hot) {
            this.raw_hot = raw_hot;
        }

        public String getNum() {
            return num;
        }

        public void setNum(String num) {
            this.num = num;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }
    }
}