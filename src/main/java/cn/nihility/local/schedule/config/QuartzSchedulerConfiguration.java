package cn.nihility.local.schedule.config;

import cn.nihility.local.schedule.properties.QuartzSchedulerProperties;
import cn.nihility.local.schedule.proxy.ProxyMethodInvokeHandler;
import org.quartz.Scheduler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.util.Properties;

/**
 * @author yuanzx
 * @date 2022/09/26 14:02
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({Scheduler.class, SchedulerFactoryBean.class})
@ConditionalOnProperty(prefix = QuartzSchedulerProperties.PREFIX, name = "enable")
@EnableConfigurationProperties(QuartzSchedulerProperties.class)
public class QuartzSchedulerConfiguration {

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(QuartzSchedulerProperties properties) {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();

        //quartz参数
        Properties prop = new Properties();
        prop.put("org.quartz.scheduler.instanceName", "QuartzScheduler");
        prop.put("org.quartz.scheduler.instanceId", "AUTO");
        //线程池配置
        prop.put("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        prop.put("org.quartz.threadPool.threadCount", properties.getThreadCount());
        prop.put("org.quartz.threadPool.threadPriority", "5");
        factory.setQuartzProperties(prop);

        factory.setSchedulerName("QuartzScheduler");
        //延时启动
        factory.setStartupDelay(10);
        factory.setApplicationContextSchedulerContextKey("applicationContextKey");
        // 可选，QuartzScheduler 启动时更新己存在的 Job，这样就不用每次修改 targetObject 后删除 qrtz_job_details 表对应记录了
        factory.setOverwriteExistingJobs(true);
        //设置自动启动，默认为 true
        factory.setAutoStartup(true);

        return factory;
    }

    @Bean
    public QuartzJobRunner quartzJobRunner(QuartzSchedulerProperties properties, Scheduler schedulerFactoryBean) {
        return new QuartzJobRunner(properties, schedulerFactoryBean);
    }

    @Bean
    public ProxyMethodInvokeHandler proxyMethodInvokeHandler() {
        return new ProxyMethodInvokeHandler();
    }

}
