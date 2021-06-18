package cn.nihility.restart.restart;

import cn.nihility.restart.RestartApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.CachedIntrospectionResults;
import org.springframework.boot.system.JavaVersion;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.beans.Introspector;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

public class Restarter extends Thread {

    private static final Logger log = LoggerFactory.getLogger(Restarter.class);

    private final String mainClassName;
    private final String[] args;
    private final Thread thread;

    private UncaughtExceptionHandler exceptionHandler;

    public Restarter(String mainClassName, String[] args, Thread thread) {
        this.mainClassName = mainClassName;
        this.args = args;
        this.thread = thread;
        exceptionHandler = thread.getUncaughtExceptionHandler();
    }

    public void immediateRestart() {
        try {
            LeakSafeThread leakSafeThread = new LeakSafeThread();
            leakSafeThread.callAndWait(() -> {
                restart();
                cleanupCaches();
                return null;
            });
        }
        catch (Exception ex) {
            log.warn("Unable to initialize restarted", ex);
        }
        SilentExitExceptionHandler.exitCurrentThread();
    }

    @Override
    public void run() {
        immediateRestart();
    }

    protected void restart() throws Exception {
        Throwable error = doStart();
        log.info("重启结果", error);
    }

    public static void main(String[] args) {
        ScanLoadLibJar scan = new ScanLoadLibJar("D:\\load_path.txt");
        scan.scan();
        Set<URL> jarUrlList = scan.getLibJarUrlSet();
        URL[] urls = jarUrlList.toArray(new URL[0]);

        URLClassLoader classLoader = new URLClassLoader(urls);
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        RestartClassLoader restart = new RestartClassLoader(urls, contextClassLoader, jarUrlList);

        final Restarter restarter = new Restarter("cn.nihility.boot.BootLearnApplication", new String[0], Thread.currentThread());
        restarter.immediateRestart();
        /*try {
            Enumeration<URL> resources = restartClassLoader.getResources("META-INF/spring.factories");
            System.out.println(resources);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                System.out.println(url);

                UrlResource urlResource = new UrlResource(url);
                System.out.println(urlResource.readResourceString());

                break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/

    }

    private Throwable doStart() throws Exception {
        if (mainClassName == null) {
            throw new IllegalArgumentException("mainClassName 不可为空");
        }

        ScanLoadLibJar scan = new ScanLoadLibJar("D:\\load_path.txt");
        scan.scan();
        Set<URL> jarUrlSet = scan.getLibJarUrlSet();
        URL[] urls = jarUrlSet.toArray(new URL[0]);

        ClassLoader parentClassLoader = thread.getContextClassLoader();
        ClassLoader classLoader = new RestartClassLoader(urls, parentClassLoader, jarUrlSet);

        log.info("Starting application [{}]", mainClassName);

        return relaunch(classLoader);
    }

    protected Throwable relaunch(ClassLoader classLoader) throws Exception {
        RestartApplicationContext.closeApplicationContext();
        RestartLauncher launcher = new RestartLauncher(classLoader, this.mainClassName,
                this.args, this.exceptionHandler);
        launcher.start();
        launcher.join();
        return launcher.getError();
    }

    private void cleanupCaches() throws Exception {
        Introspector.flushCaches();
        cleanupKnownCaches();
    }

    private void cleanupKnownCaches() throws Exception {
        // Whilst not strictly necessary it helps to cleanup soft reference caches
        // early rather than waiting for memory limits to be reached
        ResolvableType.clearCache();
        cleanCachedIntrospectionResultsCache();
        ReflectionUtils.clearCache();
        clearAnnotationUtilsCache();
        if (!JavaVersion.getJavaVersion().isEqualOrNewerThan(JavaVersion.NINE)) {
            clear("com.sun.naming.internal.ResourceManager", "propertiesCache");
        }
    }

    private void cleanCachedIntrospectionResultsCache() throws Exception {
        clear(CachedIntrospectionResults.class, "acceptedClassLoaders");
        clear(CachedIntrospectionResults.class, "strongClassCache");
        clear(CachedIntrospectionResults.class, "softClassCache");
    }

    private void clearAnnotationUtilsCache() throws Exception {
        try {
            AnnotationUtils.clearCache();
        }
        catch (Throwable ex) {
            clear(AnnotationUtils.class, "findAnnotationCache");
            clear(AnnotationUtils.class, "annotatedInterfaceCache");
        }
    }

    private void clear(String className, String fieldName) {
        try {
            clear(Class.forName(className), fieldName);
        }
        catch (Exception ex) {
            log.info("Unable to clear field [{}] - [{}]", className, fieldName, ex);
        }
    }

    private void clear(Class<?> type, String fieldName) throws Exception {
        try {
            Field field = type.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object instance = field.get(null);
            if (instance instanceof Set) {
                ((Set<?>) instance).clear();
            }
            if (instance instanceof Map) {
                ((Map<?, ?>) instance).keySet().removeIf(this::isFromRestartClassLoader);
            }
        }
        catch (Exception ex) {
            log.info("Unable to clear field [{}] - [{}]", type, fieldName, ex);
        }
    }

    private boolean isFromRestartClassLoader(Object object) {
        return (object instanceof Class
                && ((Class<?>) object).getClassLoader() instanceof RestartClassLoader);
    }

    /**
     * Thread that is created early so not to retain the {@link org.springframework.boot.devtools.restart.classloader.RestartClassLoader}.
     */
    private class LeakSafeThread extends Thread {

        private Callable<?> callable;

        private Object result;

        LeakSafeThread() {
            setDaemon(false);
        }

        public void call(Callable<?> callable) {
            this.callable = callable;
            start();
        }

        @SuppressWarnings("unchecked")
        public <V> V callAndWait(Callable<V> callable) {
            this.callable = callable;
            start();
            try {
                join();
                return (V) this.result;
            }
            catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(ex);
            }
        }

        @Override
        public void run() {
            // We are safe to refresh the ActionThread (and indirectly call
            // AccessController.getContext()) since our stack doesn't include the
            // RestartClassLoader
            try {
                this.result = this.callable.call();
            }
            catch (Exception ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        }

    }

}
