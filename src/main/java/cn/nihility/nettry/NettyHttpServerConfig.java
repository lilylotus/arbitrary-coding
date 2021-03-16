package cn.nihility.nettry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * IAM 鉴权 netty Http 服务启动入口
 */
@Configuration
public class NettyHttpServerConfig implements CommandLineRunner, ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(NettyHttpServerConfig.class);
    private static ApplicationContext applicationContext;

    @Override
    public void run(String... args) throws Exception {
        Thread nettyThread = new Thread(() -> {
            NettyHttpServer server = new NettyHttpServer(8089);
            log.info("IAM 鉴权 netty Http 服务启动");
            server.run();
        }, "Netty-Http-Server-Thread");
        nettyThread.start();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        NettyHttpServerConfig.applicationContext = applicationContext;
    }

    public static <T> List<T> getBeansByType(Class<T> type) {
        if (applicationContext != null) {
            Map<String, T> beans = applicationContext.getBeansOfType(type, false, true);
            return new ArrayList<>(beans.values());
        }
        return Collections.emptyList();
    }
}
