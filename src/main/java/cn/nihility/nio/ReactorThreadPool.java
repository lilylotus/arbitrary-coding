package cn.nihility.nio;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ReactorThreadPool {

    private ReactorThreadPool() {
    }

    private static final ExecutorService EXECUTOR = initExecutorService();

    private static ExecutorService initExecutorService() {
        final int processors = Runtime.getRuntime().availableProcessors();
        return new ThreadPoolExecutor(processors, processors * 2, 60, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(100));
    }

    public static void submit(Runnable runnable) {
        if (null != runnable) {
            EXECUTOR.submit(runnable);
        }
    }

    public static void exec(Runnable runnable) {
        if (null != runnable) {
            EXECUTOR.execute(runnable);
        }
    }

}
