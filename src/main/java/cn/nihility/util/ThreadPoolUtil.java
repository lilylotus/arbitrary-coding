package cn.nihility.util;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolUtil {

    public static ThreadPoolExecutor create(int corePoolSize, int maxPoolSize, final String serviceName) {
        return new ThreadPoolExecutor(corePoolSize, maxPoolSize, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(128),
                r -> new Thread(r, "rpc-" + serviceName + "-" + r.hashCode()),
                new ThreadPoolExecutor.AbortPolicy());
    }

}
