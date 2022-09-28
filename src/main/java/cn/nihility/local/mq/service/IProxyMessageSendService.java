package cn.nihility.local.mq.service;

import cn.nihility.local.mq.config.MessageReceiveProperties;
import cn.nihility.local.mq.config.MessageSendProperties;
import cn.nihility.local.mq.dto.LocalMessageHolder;

/**
 * @author yuanzx
 * @date 2022/09/27 14:43
 */
public interface IProxyMessageSendService {

    /**
     * 发送消息
     *
     * @param message       消息实体
     * @param senderConfig  发送消息的配置
     * @param receiveConfig 接收消息的配置
     */
    void send(LocalMessageHolder message, MessageSendProperties senderConfig, MessageReceiveProperties receiveConfig);

    /**
     * 是否支持
     *
     * @param type 发送消息的类型
     * @return true - 支持
     */
    boolean support(String type);

}
