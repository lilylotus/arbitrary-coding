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
public class MessageReceiveProperties implements Serializable {

    private static final long serialVersionUID = -5253932938358534714L;

    /**
     * 消息对应的交换器，沿用 rabbitmq 的设定
     */
    private String exchange;
    /**
     * 消费发送者 和 消息接收者 匹配的键值，沿用 rabbitmq 的设定
     */
    private String routingKey;
    /**
     * 消息消费（接收者） - 接收类
     */
    private Class<?> recClass;
    /**
     * 消息消费（接收者） - 接收类在 spring ioc 容器中对应的 bean 名称
     */
    private String recBeanName;
    /**
     * 消息消费（接收者） - 接收类调用的处理方法
     */
    private String recMethodName;
    /**
     * 消息消费（接收者） - 接收类调用方法对应的参数
     */
    private Class<?>[] recParameterTypes;
    /**
     * 消息消费（接收者） - 接收类调用方法传入的参数列表
     */
    private String[] recArgs;

}
