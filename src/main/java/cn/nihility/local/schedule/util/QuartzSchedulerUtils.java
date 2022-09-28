package cn.nihility.local.schedule.util;

import cn.nihility.local.schedule.config.QuartzSchedulerJob;
import cn.nihility.local.schedule.exception.QuartzSchedulerException;
import cn.nihility.local.schedule.properties.QuartzScheduleJob;
import org.quartz.*;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * 定时任务工具类
 *
 * @author yuanzx
 */
public class QuartzSchedulerUtils {

    private static final String JOB_NAME = "QUARTZ_JOB_";

    public static final String JOB_CONTEXT_PARAM_KEY = "jobInfo";

    private QuartzSchedulerUtils() {
    }

    /**
     * 获取触发器key
     */
    public static TriggerKey getTriggerKey(String jobId) {
        return TriggerKey.triggerKey(JOB_NAME + jobId);
    }

    /**
     * 获取jobKey
     */
    public static JobKey getJobKey(String jobId) {
        return JobKey.jobKey(JOB_NAME + jobId);
    }

    /**
     * 获取表达式触发器
     */
    public static CronTrigger getCronTrigger(Scheduler scheduler, QuartzScheduleJob job) {
        try {
            return (CronTrigger) scheduler.getTrigger(getTriggerKey(buildJobId(job)));
        } catch (SchedulerException e) {
            throw new QuartzSchedulerException("获取定时任务CronTrigger出现异常", e);
        }
    }

    public static String buildJobId(QuartzScheduleJob scheduleJob) {
        String beanName = scheduleJob.getBeanName();
        Class<?> clazz = scheduleJob.getServiceClass();
        String methodName = scheduleJob.getMethodName();
        StringJoiner sj = new StringJoiner("#");
        sj.add(Objects.toString(clazz, ""));
        sj.add(Objects.toString(beanName, ""));
        sj.add(methodName);
        return sj.toString();
    }

    /**
     * 创建定时任务
     */
    public static void createScheduleJob(Scheduler scheduler, QuartzScheduleJob scheduleJob) {
        try {
            final String jobId = buildJobId(scheduleJob);
            // 构建 job 信息
            JobDetail jobDetail = JobBuilder.newJob(QuartzSchedulerJob.class).withIdentity(getJobKey(jobId)).build();

            // 表达式调度构建器
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(scheduleJob.getCron())
                    .withMisfireHandlingInstructionDoNothing();

            // 按新的 cron 表达式构建一个新的 trigger
            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(getTriggerKey(jobId)).withSchedule(scheduleBuilder).build();

            // 放入参数，运行时的方法可以获取
            jobDetail.getJobDataMap().put(JOB_CONTEXT_PARAM_KEY, scheduleJob);

            scheduler.scheduleJob(jobDetail, trigger);

            // 暂停任务
            if (Boolean.TRUE.equals(scheduleJob.getPause())) {
                pauseJob(scheduler, jobId);
            }
        } catch (SchedulerException e) {
            throw new QuartzSchedulerException("创建定时任务失败", e);
        }
    }

    /**
     * 更新定时任务
     */
    public static void updateScheduleJob(Scheduler scheduler, QuartzScheduleJob scheduleJob) {

        try {
            final String jobId = buildJobId(scheduleJob);
            TriggerKey triggerKey = getTriggerKey(jobId);

            // 表达式调度构建器
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(scheduleJob.getCron())
                    .withMisfireHandlingInstructionDoNothing();

            CronTrigger trigger = getCronTrigger(scheduler, scheduleJob);

            // 按新的 cron 表达式重新构建 trigger
            trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();

            // 参数
            trigger.getJobDataMap().put(JOB_CONTEXT_PARAM_KEY, scheduleJob);

            scheduler.rescheduleJob(triggerKey, trigger);

            // 暂停任务
            if (Boolean.TRUE.equals(scheduleJob.getPause())) {
                pauseJob(scheduler, jobId);
            }
        } catch (SchedulerException e) {
            throw new QuartzSchedulerException("更新定时任务失败", e);
        }
    }

    /**
     * 立即执行任务
     */
    public static void run(Scheduler scheduler, QuartzScheduleJob scheduleJob) {
        try {
            //参数
            JobDataMap dataMap = new JobDataMap();
            dataMap.put(JOB_CONTEXT_PARAM_KEY, scheduleJob);
            scheduler.triggerJob(getJobKey(buildJobId(scheduleJob)), dataMap);
        } catch (SchedulerException e) {
            throw new QuartzSchedulerException("立即执行定时任务失败", e);
        }
    }

    /**
     * 暂停任务
     */
    public static void pauseJob(Scheduler scheduler, String jobId) {
        try {
            scheduler.pauseJob(getJobKey(jobId));
        } catch (SchedulerException e) {
            throw new QuartzSchedulerException("暂停定时任务失败", e);
        }
    }

    /**
     * 恢复任务
     */
    public static void resumeJob(Scheduler scheduler, String jobId) {
        try {
            scheduler.resumeJob(getJobKey(jobId));
        } catch (SchedulerException e) {
            throw new QuartzSchedulerException("暂停定时任务失败", e);
        }
    }

    /**
     * 删除定时任务
     */
    public static void deleteScheduleJob(Scheduler scheduler, String jobId) {
        try {
            scheduler.deleteJob(getJobKey(jobId));
        } catch (SchedulerException e) {
            throw new QuartzSchedulerException("删除定时任务失败", e);
        }
    }
}
