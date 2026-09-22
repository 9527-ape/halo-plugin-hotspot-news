package run.hotspotnews.web;

import java.nio.charset.StandardCharsets;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import run.halo.app.security.AdditionalWebFilter;

/**
 * 摸鱼页面短链入口：将 {@code GET /moyu} 直接渲染为热点摸鱼页面。
 *
 * <p>通过 Halo 官方的 {@code AdditionalWebFilter} 扩展点注册，
 * 不进入 RBAC 的 /apis 路由体系，站点匿名访客可直接访问。</p>
 */
@Component
public class MoyuRouteFilter implements AdditionalWebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        if (HttpMethod.GET.equals(exchange.getRequest().getMethod())
            && ("/moyu".equals(path) || "/moyu/".equals(path))) {
            var response = exchange.getResponse();
            response.setStatusCode(HttpStatus.OK);
            response.getHeaders()
                .setContentType(new MediaType(MediaType.TEXT_HTML, StandardCharsets.UTF_8));
            byte[] body = NewsPage.HTML.getBytes(StandardCharsets.UTF_8);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(body)));
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}