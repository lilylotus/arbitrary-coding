package cn.nihility.restart.reload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class ReloadClassLoaderThread extends Thread {

    private static final Logger log = LoggerFactory.getLogger(ReloadClassLoaderThread.class);

    private final String mainClassName;
    private final String[] args;

    private Throwable error;

    public ReloadClassLoaderThread(String mainClassName, String[] args, ClassLoader classLoader) {
        this.mainClassName = mainClassName;
        this.args = args;

        setName("reloadClassMain");
        setDaemon(false);
        setContextClassLoader(classLoader);
    }

    @Override
    public void run() {
        reload();
    }

    public void reload() {
        try {
            Class<?> mainClass = getContextClassLoader().loadClass(this.mainClassName);
            Method mainMethod = mainClass.getDeclaredMethod("main", String[].class);
            mainMethod.invoke(null, new Object[] { this.args });
        } catch (Throwable ex) {
            this.error = ex;
        }
    }

    public Throwable getError() {
        return error;
    }
}
