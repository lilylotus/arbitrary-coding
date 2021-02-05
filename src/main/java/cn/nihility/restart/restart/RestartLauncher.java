package cn.nihility.restart.restart;

import java.lang.reflect.Method;

public class RestartLauncher extends Thread {

    private final String mainClassName;

    private final String[] args;

    private Throwable error;

    RestartLauncher(ClassLoader classLoader, String mainClassName, String[] args,
                    UncaughtExceptionHandler exceptionHandler) {
        this.mainClassName = mainClassName;
        this.args = args;
        setName("restartedMain");
        setUncaughtExceptionHandler(exceptionHandler);
        setDaemon(false);
        setContextClassLoader(classLoader);
    }

    @Override
    public void run() {
        try {
            Class<?> mainClass = getContextClassLoader().loadClass(this.mainClassName);
            Method mainMethod = mainClass.getDeclaredMethod("main", String[].class);
            mainMethod.invoke(null, new Object[] { this.args });
        }
        catch (Throwable ex) {
            this.error = ex;
            getUncaughtExceptionHandler().uncaughtException(this, ex);
        }
    }

    public Throwable getError() {
        return this.error;
    }

}
