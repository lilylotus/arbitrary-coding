package cn.nihility.controller.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.ServletRequestHandledEvent;

@Component
public class ServletRequestHandledEventListener implements ApplicationListener<ServletRequestHandledEvent> {

    private static final Logger log = LoggerFactory.getLogger(ServletRequestHandledEventListener.class);

    @Override
    public void onApplicationEvent(ServletRequestHandledEvent event) {
        log.debug("清除当前线程用户信息,uri = [{}], method = [{}], servletName = [{}], clientAddress = [{}]",
            event.getRequestUrl(), event.getMethod(), event.getServletName(), event.getClientAddress());
    }

}
