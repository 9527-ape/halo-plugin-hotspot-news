package run.hotspotnews.config;

/**
 * 插件配置。与 {@code extensions/settings.yaml} 中 group 为 {@code basic} 的表单一一对应。
 *
 * <p>通过 {@code ReactiveSettingFetcher.fetch("basic", PluginSetting.class)} 读取，
 * 所有字段均可为空，使用时需通过本类提供的带默认值方法获取生效值。</p>
 */
public record PluginSetting(
    Boolean weiboEnabled,
    Boolean lianboEnabled,
    Integer intervalMinutes,
    Integer maxItemsPerSource,
    Integer minHotValue,
    String weiboApiUrl,
    String lianboPageUrl,
    String userAgent
) {

    public static final String GROUP = "basic";

    public static final String SOURCE_WEIBO = "WEIBO";
    public static final String SOURCE_LIANBO = "XINWEN_LIANBO";

    public static PluginSetting defaultSetting() {
        return new PluginSetting(
            true,
            true,
            30,
            50,
            0,
            "https://weibo.com/ajax/statuses/hot_band",
            "https://tv.cctv.com/lm/xwlb/",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) "
                + "Chrome/126.0.0.0 Safari/537.36"
        );
    }

    public boolean weiboActive() {
        return weiboEnabled == null || weiboEnabled;
    }

    public boolean lianboActive() {
        return lianboEnabled == null || lianboEnabled;
    }

    public int syncInterval() {
        return intervalMinutes == null ? 30 : Math.max(5, intervalMinutes);
    }

    public int maxItems() {
        return maxItemsPerSource == null ? 20 : maxItemsPerSource;
    }

    public int minHot() {
        return minHotValue == null ? 0 : Math.max(0, minHotValue);
    }

    public String weiboApi() {
        return isBlank(weiboApiUrl) ? defaultSetting().weiboApiUrl : weiboApiUrl.trim();
    }

    public String lianboPage() {
        return isBlank(lianboPageUrl) ? defaultSetting().lianboPageUrl : lianboPageUrl.trim();
    }

    public String ua() {
        return isBlank(userAgent) ? defaultSetting().userAgent : userAgent.trim();
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}