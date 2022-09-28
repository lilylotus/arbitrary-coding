package cn.nihility.local.mq.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author yuanzx
 * @date 2022/09/27 14:59
 */
@Setter
@Getter
@ToString
public class MessageSendProperties implements Serializable {

    private static final long serialVersionUID = 6667683338730183762L;

    /**
     * 消息对应的交换器，沿用 rabbitmq 的设定
     */
    private String exchange;
    /**
     * 消费发送者 和 消息接收者 匹配的键值，沿用 rabbitmq 的设定
     */
    private String routingKey;
    /**
     * 消息生产者 - 发送类
     */
    private Class<?> sendClass;
    /**
     * 消息生产者 - 发送类在 spring ioc 容器中的名称
     */
    private String sendBeanName;
    /**
     * 消息生产者 - 发送类调用的方法
     */
    private String sendMethod;
    /**
     * 消息生产者 - 发送类调用的方法所对应的参数类型列表
     */
    private Class<?>[] sendParameterTypes;
    /**
     * 消息生产者 - 发送参数的保存位置，键值
     */
    private String[] sendArgs;

}
