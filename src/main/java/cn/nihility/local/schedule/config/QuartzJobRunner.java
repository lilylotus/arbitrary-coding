package cn.nihility.local.schedule.config;

import cn.nihility.local.schedule.properties.QuartzScheduleJob;
import cn.nihility.local.schedule.properties.QuartzSchedulerProperties;
import cn.nihility.local.schedule.util.QuartzSchedulerUtils;
import org.quartz.CronTrigger;
import org.quartz.Scheduler;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.util.List;

/**
 * @author yuanzx
 * @date 2022/09/26 14:33
 */
public class QuartzJobRunner implements ApplicationRunner {

    private QuartzSchedulerProperties properties;
    private Scheduler scheduler;

    public QuartzJobRunner(QuartzSchedulerProperties properties, Scheduler schedulerFactoryBean) {
        this.properties = properties;
        this.scheduler = schedulerFactoryBean;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        List<QuartzScheduleJob> jobs = properties.getJobs();

        for (QuartzScheduleJob job : jobs) {
            CronTrigger cronTrigger = QuartzSchedulerUtils.getCronTrigger(scheduler, job);
            //如果不存在，则创建
            if (cronTrigger == null) {
                QuartzSchedulerUtils.createScheduleJob(scheduler, job);
            } else {
                QuartzSchedulerUtils.updateScheduleJob(scheduler, job);
            }
        }

    }

}
