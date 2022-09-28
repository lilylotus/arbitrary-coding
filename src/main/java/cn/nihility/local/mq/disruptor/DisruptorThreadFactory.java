package cn.nihility.local.mq.disruptor;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yuanzx
 * @date 2022/09/22 16:59
 */
public class DisruptorThreadFactory implements ThreadFactory {

    private static final AtomicInteger FACTORY_INDEX = new AtomicInteger();

    private final AtomicInteger nextId = new AtomicInteger();

    private final String prefix;

    public DisruptorThreadFactory() {
        this.prefix = FACTORY_INDEX.getAndIncrement() + "-";
    }

    public DisruptorThreadFactory(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, "disruptor-" + prefix + nextId.getAndIncrement());
    }

}
