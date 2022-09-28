package cn.nihility.local.mq.redis;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yuanzx
 * @date 2022/09/28 11:20
 */
public class RedisMessageThreadFactory implements ThreadFactory {

    private static final String PREFIX = "redisMQ-";

    private static final AtomicInteger INDEX = new AtomicInteger(0);

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, PREFIX + INDEX.getAndIncrement());
    }

}
