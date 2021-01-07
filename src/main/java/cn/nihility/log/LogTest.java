package cn.nihility.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogTest {

    private static final Logger log = LoggerFactory.getLogger(LogTest.class);


    public static void main(String[] args) {
        String msg = "This is logger test message.";

        System.setProperty("appName", "exec");
        System.setProperty("profileName", "exec");

        log.trace("Log Test message [{}]", msg);
        log.debug("Log Test message [{}]", msg);
        log.info("Log Test message [{}]", msg);
        log.warn("Log Test message [{}]", msg);
        log.error("Log Test message [{}]", msg);
        /*while (true) {
            log.trace("Log Test message [{}]", msg);
            log.debug("Log Test message [{}]", msg);
            log.info("Log Test message [{}]", msg);
            log.warn("Log Test message [{}]", msg);
            log.error("Log Test message [{}]", msg);
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/
    }


}
