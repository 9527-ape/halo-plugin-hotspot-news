package run.hotspotnews.web;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.hotspotnews.service.NewsQueryService;
import run.hotspotnews.service.NewsSyncService;

/**
 * 热点新闻对外接口。
 *
 * <p>使用 Halo 约定的插件公开 API 组 {@code api.plugin.halo.run}：
 * 该组的 GET/list 接口在 Halo 内置的 {@code role-template-public-apis}
 * 中已对匿名用户开放，站点访客无需登录即可读取数据。</p>
 *
 * <p>最终路径前缀为 {@code /apis/api.plugin.halo.run/v1alpha1}：</p>
 * <ul>
 *   <li>GET /hotspot-news —— 聚合新闻列表，支持 ?source=weibo|lianbo|all（匿名可访问）</li>
 *   <li>GET /hotspot-news/status —— 同步状态（匿名可访问）</li>
 *   <li>POST /hotspot-news/refresh —— 手动触发一次同步（需要登录，管理员）</li>
 * </ul>
 * <p>开心一刻页面入口为短链 {@code /moyu}，由 {@link MoyuRouteFilter} 直接渲染。</p>
 */
@Component
public class NewsEndpoint implements CustomEndpoint {

    private final NewsSyncService syncService;
    private final NewsQueryService queryService;

    public NewsEndpoint(NewsSyncService syncService, NewsQueryService queryService) {
        this.syncService = syncService;
        this.queryService = queryService;
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        return RouterFunctions.route()
            .GET("/hotspot-news", this::listNews)
            .GET("/hotspot-news/status", this::status)
            .POST("/hotspot-news/refresh", this::refresh)
            .build();
    }

    @Override
    public GroupVersion groupVersion() {
        return new GroupVersion("api.plugin.halo.run", "v1alpha1");
    }

    private Mono<ServerResponse> listNews(ServerRequest request) {
        String source = request.queryParam("source").orElse("all");
        return queryService.listItems(source)
            .flatMap(items -> ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .bodyValue(items));
    }

    private Mono<ServerResponse> refresh(ServerRequest request) {
        return syncService.syncNow()
            .then(syncService.currentStatus())
            .flatMap(status -> ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .bodyValue(status));
    }

    private Mono<ServerResponse> status(ServerRequest request) {
        return syncService.currentStatus()
            .flatMap(status -> ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .bodyValue(status));
    }
}