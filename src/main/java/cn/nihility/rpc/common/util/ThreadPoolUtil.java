package cn.nihility.rpc.common.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class ThreadPoolUtil {

    private static final ThreadPoolExecutor CLIENT_EXECUTOR = new ThreadPoolExecutor(4, 8, 60L, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(100),
            new ThreadFactory() {
                private final AtomicInteger count = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "RpcClient [" + count.getAndIncrement() + "]");
                }
            });

    private static final ThreadPoolExecutor SERVER_EXECUTOR = new ThreadPoolExecutor(4, 8, 60L, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(100),
            new ThreadFactory() {
                private final AtomicInteger count = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "RpcServer [" + count.getAndIncrement() + "]");
                }
            });

    private ThreadPoolUtil() {
    }

    public static void clientSubmit(Runnable runnable) {
        CLIENT_EXECUTOR.submit(runnable);
    }

    public static void serverSubmit(Runnable runnable) {
        SERVER_EXECUTOR.submit(runnable);
    }

    public static void serverExecute(Runnable runnable) {
        SERVER_EXECUTOR.execute(runnable);
    }

    public static void shutdown() {
        SERVER_EXECUTOR.shutdown();
        CLIENT_EXECUTOR.shutdown();
    }

}
