package run.hotspotnews.dto;

import run.hotspotnews.model.HotspotNewsItem;

/**
 * 对外输出的新闻条目（API 响应体），避免暴露自定义模型内部细节。
 */
public record NewsFeedItem(
    String id,
    String source,
    String title,
    String summary,
    String url,
    String category,
    Integer hotValue,
    Integer rank,
    String publishedDate,
    String fetchedAt
) {

    public static NewsFeedItem from(HotspotNewsItem item) {
        var spec = item.getSpec();
        return new NewsFeedItem(
            item.getMetadata().getName(),
            spec.getSource(),
            spec.getTitle(),
            spec.getSummary(),
            spec.getUrl(),
            spec.getCategory(),
            spec.getHotValue(),
            spec.getRank(),
            spec.getPublishedDate(),
            spec.getFetchedAt()
        );
    }
}