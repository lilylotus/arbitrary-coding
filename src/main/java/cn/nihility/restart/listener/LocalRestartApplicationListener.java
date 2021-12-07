package cn.nihility.restart.listener;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.ClassUtils;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * META-INF/spring.factories
 * org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
 * cn.nihility.restart.listener.LocalRestartApplicationListener
 */
public class LocalRestartApplicationListener implements ApplicationListener<ApplicationPreparedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(LocalRestartApplicationListener.class);

    private ApplicationPreparedEvent event;

    private ConfigurableApplicationContext context;

    private String[] args;

    private SpringApplication application;

    @Override
    public void onApplicationEvent(ApplicationPreparedEvent apEvent) {
        logger.info("Restart Application Listener Registered.");
        this.event = apEvent;
        if (this.context == null) {
            this.context = event.getApplicationContext();
            this.args = event.getArgs();
            this.application = event.getSpringApplication();
        }
    }

    public Map<String, String> restart() {
        Thread thread = new Thread(this::safeRestart);
        thread.setDaemon(false);
        thread.start();
        return Collections.singletonMap("message", "Restarting");
    }

    private boolean safeRestart() {

        try {
            logger.info("Do restarting");
            doRestart();
            logger.info("Restarted");
            return true;
        } catch (Exception e) {
            logger.info("Could not doRestart", e);
            return false;
        }

    }

    private synchronized ConfigurableApplicationContext doRestart() {

        if (context != null) {
            //application.setEnvironment(context.getEnvironment());
            close();
            // If running in a webapp then the context classloader is probably going to
            // die so we need to revert to a safe place before starting again
            overrideClassLoaderForRestart();
            context = application.run(args);
        }

        return context;
    }

    private void overrideClassLoaderForRestart() {
        ClassUtils.overrideThreadContextClassLoader(application.getClass().getClassLoader());
    }

    private void close() {

        ApplicationContext ctx = this.context;
        while (ctx instanceof Closeable) {
            try {
                ((Closeable) ctx).close();
            }
            catch (IOException e) {
                logger.error("Cannot close context: " + ctx.getId(), e);
            }
            ctx = ctx.getParent();
        }

    }

}
