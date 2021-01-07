package cn.nihility.aware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class MyApplicationContextAware implements ApplicationContextAware {

    private static ApplicationContext applicationContext;
    private final static Logger log = LoggerFactory.getLogger(MyApplicationContextAware.class);

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        log.info("MyApplicationContextAware Set ApplicationContext");
        MyApplicationContextAware.applicationContext = applicationContext;
    }

    public static <T> T getBean(Class<T> clazz) {
        log.info("MyApplicationContextAware getBean [{}]", clazz.getName());
        return applicationContext.getBean(clazz);
    }

    public static Object getBean(String beanName) {
        log.info("MyApplicationContextAware getBean [{}]", beanName);
        return applicationContext.getBean(beanName);
    }

    public static <T> T getBean(String beanName, Class<T> type) {
        log.info("MyApplicationContextAware getBean name [{}], type [{}]", beanName, type.getName());
        return applicationContext.getBean(beanName, type);
    }

}
