package cn.nihility.local.mq.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 消息队列配置参数
 *
 * @author yuanzx
 * @date 2022/09/27 11:03
 */
@Getter
@Setter
@ToString
@ConfigurationProperties(prefix = "mq.local")
public class MessageConfigurationProperties {

    /**
     * 是否启动
     */
    private Boolean enable;

    /**
     * queue 的类型，disruptor / redis
     */
    private String type;

    /**
     * 定义 mq 发送的配置信息
     */
    private List<MessageSendProperties> senders = new ArrayList<>();

    /**
     * 定义 mq 接收的配置信息
     */
    private List<MessageReceiveProperties> receivers = new ArrayList<>();


}
