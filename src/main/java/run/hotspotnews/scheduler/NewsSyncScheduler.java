package run.hotspotnews.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.hotspotnews.service.NewsSyncService;

/**
 * 定时同步调度器。
 *
 * <p>每 5 分钟检查一次，是否真正执行由 {@link NewsSyncService#syncIfNeeded()}
 * 依据配置的同步间隔决定。任务注册在插件自身的应用上下文中，插件停止时自动取消。</p>
 */
@Component
public class NewsSyncScheduler {

    private final NewsSyncService syncService;

    public NewsSyncScheduler(NewsSyncService syncService) {
        this.syncService = syncService;
    }

    @Scheduled(fixedDelay = 5 * 60 * 1000L, initialDelay = 20 * 1000L)
    public Mono<Void> tick() {
        return syncService.syncIfNeeded();
    }
}