package run.hotspotnews.config;

import java.util.List;

/**
 * 插件配置。与 {@code extensions/settings.yaml} 中 group 为 {@code basic} 的表单一一对应。
 *
 * <p>通过 {@code ReactiveSettingFetcher.fetch("basic", PluginSetting.class)} 读取，
 * 所有字段均可为空，使用时需通过本类提供的带默认值方法获取生效值。</p>
 */
public record PluginSetting(
    Boolean jokeEnabled,
    Boolean soupEnabled,
    Integer intervalMinutes,
    Integer maxItemsPerSource,
    String soupApiBase,
    String userAgent
) {

    public static final String GROUP = "basic";

    public static final String SOURCE_JOKE = "JOKE";
    public static final String SOURCE_SOUP = "SOUP";

    /** 一言句子库分类：d=文学，i=诗词，k=哲学 */
    public static final List<String> DEFAULT_SOUP_CATEGORIES = List.of("d", "i", "k");

    public static PluginSetting defaultSetting() {
        return new PluginSetting(
            true,
            true,
            30,
            50,
            "https://cdn.jsdelivr.net/gh/hitokoto-osc/sentences-bundle@master/sentences/",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) "
                + "Chrome/126.0.0.0 Safari/537.36"
        );
    }

    public boolean jokeActive() {
        return jokeEnabled == null || jokeEnabled;
    }

    public boolean soupActive() {
        return soupEnabled == null || soupEnabled;
    }

    public int syncInterval() {
        return intervalMinutes == null ? 30 : Math.max(5, intervalMinutes);
    }

    public int maxItems() {
        return maxItemsPerSource == null ? 20 : Math.max(1, maxItemsPerSource);
    }

    /** 一言句子库 CDN 基地址（以 / 结尾）。 */
    public String soupApiBase() {
        if (soupApiBase == null || soupApiBase.isBlank()) {
            return defaultSetting().soupApiBase;
        }
        String base = soupApiBase.trim();
        return base.endsWith("/") ? base : base + "/";
    }

    public List<String> soupCategories() {
        return DEFAULT_SOUP_CATEGORIES;
    }

    public String ua() {
        return userAgent == null || userAgent.isBlank()
            ? defaultSetting().userAgent : userAgent.trim();
    }
}