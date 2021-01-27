package cn.nihility.restart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestartApplicationContext implements ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(RestartApplicationContext.class);

    private static ConfigurableApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        RestartApplicationContext.applicationContext = (ConfigurableApplicationContext) applicationContext;
        log.info("重启获取 spring 上下文成功");
    }

    public static void closeApplicationContext() {
        if (null != applicationContext) {
            applicationContext.close();
        }
    }

}
