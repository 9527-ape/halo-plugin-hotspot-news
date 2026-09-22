package run.hotspotnews.model;

import io.swagger.v3.oas.annotations.media.Schema;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * 热点新闻条目，持久化保存每次同步到的微博热搜与《新闻联播》内容。
 *
 * <p>注册后 Halo 会自动提供如下 RESTful API：</p>
 * <pre>
 * GET    /apis/news.moyu.run/v1alpha1/hotspotnewsitems
 * GET    /apis/news.moyu.run/v1alpha1/hotspotnewsitems/{name}
 * POST   /apis/news.moyu.run/v1alpha1/hotspotnewsitems
 * PUT    /apis/news.moyu.run/v1alpha1/hotspotnewsitems/{name}
 * DELETE /apis/news.moyu.run/v1alpha1/hotspotnewsitems/{name}
 * </pre>
 */
@GVK(
    group = "news.moyu.run",
    version = "v1alpha1",
    kind = "HotspotNewsItem",
    plural = "hotspotnewsitems",
    singular = "hotspotnewsitem"
)
public class HotspotNewsItem extends AbstractExtension {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Spec spec;

    public Spec getSpec() {
        return spec;
    }

    public void setSpec(Spec spec) {
        this.spec = spec;
    }

    @Schema(name = "HotspotNewsItemSpec")
    public static class Spec {

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 200)
        private String title;

        /** 来源：WEIBO / XINWEN_LIANBO */
        @Schema(maxLength = 64)
        private String source;

        @Schema(maxLength = 2000)
        private String url;

        @Schema(maxLength = 1000)
        private String summary;

        @Schema(maxLength = 64)
        private String category;

        @Schema(maxLength = 32)
        private String publishedDate;

        @Schema(maximum = "2147483647")
        private Integer hotValue;

        private Integer rank;

        @Schema(maxLength = 64)
        private String fetchedAt;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getPublishedDate() {
            return publishedDate;
        }

        public void setPublishedDate(String publishedDate) {
            this.publishedDate = publishedDate;
        }

        public Integer getHotValue() {
            return hotValue;
        }

        public void setHotValue(Integer hotValue) {
            this.hotValue = hotValue;
        }

        public Integer getRank() {
            return rank;
        }

        public void setRank(Integer rank) {
            this.rank = rank;
        }

        public String getFetchedAt() {
            return fetchedAt;
        }

        public void setFetchedAt(String fetchedAt) {
            this.fetchedAt = fetchedAt;
        }
    }
}