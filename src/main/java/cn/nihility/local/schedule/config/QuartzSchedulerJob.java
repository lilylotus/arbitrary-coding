package cn.nihility.local.schedule.config;

import cn.nihility.local.schedule.properties.QuartzScheduleJob;
import cn.nihility.local.schedule.proxy.ProxyMethodInvokeHandler;
import cn.nihility.local.schedule.util.QuartzSchedulerUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * @author yuanzx
 * @date 2022/09/26 15:05
 */
public class QuartzSchedulerJob extends QuartzJobBean {

    private static final Logger log = LoggerFactory.getLogger(QuartzSchedulerJob.class);

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {

        QuartzScheduleJob scheduleJob = (QuartzScheduleJob) context.getMergedJobDataMap()
                .get(QuartzSchedulerUtils.JOB_CONTEXT_PARAM_KEY);

        log.info("invoke [{}]", scheduleJob.getBeanName());

        try {
            ProxyMethodInvokeHandler.proxyInvoke(scheduleJob);
        } catch (Exception ex) {
            log.error("Quartz执行任务异常", ex);
        }

    }

}
