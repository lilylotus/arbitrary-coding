package cn.nihility.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * corn 表达式
 * 秒(0-60) 分(0-60) 时(0-24) 日(0-31) 月(0-12) 周 年
 * 仅 日 和 周 可以使用 ? 号，两个字段仅有一个能用（互斥），那个字段不用就设置为 ?
 * 0 0 12 * * ? （每天 12 时执行任务）
 *
 * {@link Scheduled} 源码注释：
 * 每一个有 @Scheduled 注解的方法都会被注册为一个 {@link ScheduledAnnotationBeanPostProcessor#setScheduler(java.lang.Object)}
 * 若是不主动配置所需的 {@link TaskScheduler}
 * SpringBoot 会默认使用一个单线程的 scheduler 来处理 @Scheduled 注解实现的定时任务
 *
 * 注意： @Scheduled 定时任务方法配置在 @Component 注解的类中，或中 @Bean 新建
 *
 */
@Component
public class SchedulerTask {

    private final static Logger log = LoggerFactory.getLogger(SchedulerTask.class);

    @Async("threadPoolTaskExecutor1")
    @Scheduled(cron = "0 0/1 * * * ?")
    public void schedulePrint() {
        log.info("Schedule Task Print, peer 1 minute");
    }

    @Async("threadPoolTaskExecutor2")
    @Scheduled(cron = "0/30 * * * * ?")
    public void schedulePrint2() {
        log.info("Schedule Task Print, peer 30 seconds");
    }

}
