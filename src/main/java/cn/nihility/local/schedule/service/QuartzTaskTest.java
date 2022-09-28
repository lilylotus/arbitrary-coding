package cn.nihility.local.schedule.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author intel
 * @date 2022/09/26 14:28
 */
@Service
public class QuartzTaskTest {

    private static final Logger log = LoggerFactory.getLogger(QuartzTaskTest.class);

    //    @Scheduled(cron = "0/10 * * * * ?")
    public void schedule(String param) {
        log.info("Quartz Scheduler Test Task invoked [{}]", param);
    }

    public void schedule(String param, int count) {
        log.info("Quartz Scheduler Test Task invoked [{}]", param);
    }

}
