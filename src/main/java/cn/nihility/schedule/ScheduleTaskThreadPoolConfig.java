package cn.nihility.schedule;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 1. 没有配置自定义线程池时，会默认使用 SimpleAsyncTaskExecutor。
 * 2. 只配置了一个线程池，不需要显示指定使用这个线程池，
 *    spring 会自动使用该唯一的线程池，但如果配置了多个就必须要显示指定，否则还是会使用默认的。
 * 3. 如果想要指定使用哪个线程池，使用 @Async("executor2") 显示指定。
 */
@Configuration
@EnableScheduling
@EnableAsync
public class ScheduleTaskThreadPoolConfig {

    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor1() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(1024);
        executor.setThreadNamePrefix("scheduler1-");
        return executor;
    }

    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor2() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(1024);
        executor.setThreadNamePrefix("scheduler2-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }

    /*@Configuration
    static class SchedulingConfigurerImpl implements SchedulingConfigurer {
        @Override
        public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

            executor.setCorePoolSize(4);
            executor.setMaxPoolSize(4);
            executor.setQueueCapacity(1024);
            executor.setThreadNamePrefix("schedulerPool-");

            scheduler.setThreadFactory(executor);
            scheduler.initialize();

            taskRegistrar.setTaskScheduler(scheduler);
        }
    }*/

}
