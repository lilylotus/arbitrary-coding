package cn.nihility.local.mq.util;

import cn.nihility.local.mq.config.MessageReceiveProperties;
import cn.nihility.local.mq.disruptor.DisruptorThreadFactory;
import cn.nihility.local.mq.disruptor.MessageHolderEventFactory;
import cn.nihility.local.mq.disruptor.MessageHolderEventHandler;
import cn.nihility.local.mq.dto.LocalMessageHolder;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.concurrent.ThreadFactory;

/**
 * Disruptor 内存消息队列操作工具类
 *
 * @author yuanzx
 * @date 2022/09/27 11:11
 */
public class DisruptorUtils {

    private static final int RING_BUFFER_SIZE = 2 << 14;

    private DisruptorUtils() {
    }

    /**
     * disruptor 消息队列构建
     *
     * @param receiveConfig 该队列的处理器配置信息
     * @return 消息队列
     */
    public static RingBuffer<LocalMessageHolder> build(MessageReceiveProperties receiveConfig) {
        if (null == receiveConfig) {
            return null;
        }
        MessageHolderEventFactory factory = new MessageHolderEventFactory();
        DisruptorThreadFactory threadFactory = new DisruptorThreadFactory();
        MessageHolderEventHandler handler = new MessageHolderEventHandler(receiveConfig);
        return build(factory, threadFactory, handler);
    }

    /**
     * 构建 disruptor mq 消息队列
     *
     * @param eventFactory  生成消息对象的工厂
     * @param threadFactory 线程工厂
     * @param eventHandler  处理队列消息的处理器
     * @param <T>           处理的消息类型
     * @return disruptor 的消息队列
     */
    public static <T> RingBuffer<T> build(EventFactory<T> eventFactory, ThreadFactory threadFactory, EventHandler<T> eventHandler) {
        Disruptor<T> disruptor = new Disruptor<>(eventFactory, RING_BUFFER_SIZE,
                threadFactory, ProducerType.MULTI, new BlockingWaitStrategy());
        // 设置消息处理器 - 消费者
        disruptor.handleEventsWith(eventHandler);
        // 启动 ring 环，用于接收生产者消息
        return disruptor.start();
    }

}
