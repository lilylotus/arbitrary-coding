package cn.nihility.boot;

import org.apache.catalina.connector.Connector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 优雅的关闭 SPRING BOOT 服务
 */
@Configuration
public class GracefulShutdownConfiguration implements ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(GracefulShutdownConfiguration.class);
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        GracefulShutdownConfiguration.applicationContext = applicationContext;
    }

    public static void shutdown() {
        if (GracefulShutdownConfiguration.applicationContext != null) {
            log.info("shutdown() 发出关闭 Spring Boot 服务的信号，正在关闭服务");
            int exitCode = SpringApplication.exit(applicationContext, () -> 0);
            log.info("shutdown() 退出状态码 [{}]", exitCode);
        }
    }

    public static void close() {
        if (applicationContext instanceof ConfigurableApplicationContext) {
            log.info("close() 发出关闭 Spring Boot 服务的信号，正在关闭服务");
            ConfigurableApplicationContext closable = (ConfigurableApplicationContext) applicationContext;
            closable.close();
        }
    }

    @Bean
    public GracefulShutdown gracefulShutdown() {
        return new GracefulShutdown();
    }

    @Bean
    public ServletWebServerFactory gracefulShutdownServletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        tomcat.addConnectorCustomizers(gracefulShutdown());
        return tomcat;
    }

    private static class GracefulShutdown implements TomcatConnectorCustomizer, ApplicationListener<ContextClosedEvent> {
        private static final Logger log = LoggerFactory.getLogger(GracefulShutdown.class);

        @Value("${graceful.shutdown.waitTime:30}")
        private int waitTime;
        private volatile Connector connector;

        @Override
        public void customize(Connector connector) {
            this.connector = connector;
        }

        @Override
        public void onApplicationEvent(ContextClosedEvent event) {
            log.info("application is going to stop. try to stop tomcat gracefully after [{}] seconds", waitTime);
            this.connector.pause();
            Executor executor = this.connector.getProtocolHandler().getExecutor();
            if (executor instanceof ThreadPoolExecutor) {
                ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executor;
                threadPoolExecutor.shutdown();
                try {
                    if (!threadPoolExecutor.awaitTermination(waitTime, TimeUnit.SECONDS)) {
                        log.info("Tomcat did not terminate in the specified time.");
                        threadPoolExecutor.shutdownNow();
                    }
                } catch (Exception ex) {
                    log.error("awaitTermination failed.", ex);
                    threadPoolExecutor.shutdownNow();
                }
            }
        }
    }
}
