package run.hotspotnews;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.SchemeManager;
import run.halo.app.plugin.BasePlugin;
import run.halo.app.plugin.PluginContext;
import run.hotspotnews.model.HotspotNewsItem;

/**
 * 热点摸鱼插件入口。
 *
 * <p>插件启动时注册自定义模型 {@link HotspotNewsItem} 的 Scheme，停止时对称注销。
 * Scheme 注销不会删除已持久化的业务数据，重新启用后仍可继续读取。</p>
 */
@Component
@EnableScheduling
public class HotspotNewsPlugin extends BasePlugin {

    private final SchemeManager schemeManager;

    public HotspotNewsPlugin(PluginContext pluginContext, SchemeManager schemeManager) {
        super(pluginContext);
        this.schemeManager = schemeManager;
    }

    @Override
    public void start() {
        schemeManager.register(HotspotNewsItem.class);
    }

    @Override
    public void stop() {
        schemeManager.unregister(Scheme.buildFromType(HotspotNewsItem.class));
    }
}