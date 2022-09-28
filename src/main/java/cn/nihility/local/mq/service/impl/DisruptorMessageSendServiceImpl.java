package cn.nihility.local.mq.service.impl;

import cn.nihility.local.mq.config.MessageReceiveProperties;
import cn.nihility.local.mq.config.MessageSendProperties;
import cn.nihility.local.mq.disruptor.MessageHolderEventTranslator;
import cn.nihility.local.mq.dto.LocalMessageHolder;
import cn.nihility.local.mq.service.IProxyMessageSendService;
import cn.nihility.local.mq.util.DisruptorUtils;
import com.lmax.disruptor.RingBuffer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 采用 disruptor 来发送消息队列消息
 *
 * @author yuanzx
 * @date 2022/09/27 14:46
 */
public class DisruptorMessageSendServiceImpl implements IProxyMessageSendService {

    private final Map<String, RingBuffer<LocalMessageHolder>> disruptorRingBuffer = new ConcurrentHashMap<>(16);

    private static final String DISRUPTOR_TYPE = "disruptor";

    /**
     * 获取指定路由 key 的 disruptor 的消息队列
     *
     * @param senderConfig  发送消息的配置
     * @param receiveConfig 接收消息的配置
     * @return 消息队列
     */
    private RingBuffer<LocalMessageHolder> obtainRingBuffer(MessageSendProperties senderConfig, MessageReceiveProperties receiveConfig) {
        return disruptorRingBuffer.computeIfAbsent(senderConfig.getRoutingKey(), c -> DisruptorUtils.build(receiveConfig));
    }

    @Override
    public void send(LocalMessageHolder message, MessageSendProperties senderConfig, MessageReceiveProperties receiveConfig) {

        obtainRingBuffer(senderConfig, receiveConfig).publishEvent(new MessageHolderEventTranslator(), message);

    }

    @Override
    public boolean support(String type) {
        return DISRUPTOR_TYPE.equals(type);
    }

}
