package cn.nihility.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 持久化消息
 */
public class DurableProducer {

    private final static String EXCHANGE_NAME = "durable-exchange";
    private final static String QUEUE_NAME = "durable-queue";

    public static void main(String[] args) throws IOException, TimeoutException {
        // 创建一个连接
        Connection connection = RabbitUtil.obtainConnection();
        // 创建一个通道
        Channel channel = connection.createChannel();
        // 创建一个 Exchange, 是否 durable = true
        channel.exchangeDeclare(EXCHANGE_NAME, "direct", true);
        // 创建一个队列
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
        // 把队列和 exchange 绑定
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "");

        // 发送消息
        String message = "durable exchange test";
        AMQP.BasicProperties props = new AMQP.BasicProperties().builder().deliveryMode(2).build();
        channel.basicPublish(EXCHANGE_NAME, "", props, message.getBytes());

        // 关闭频道和连接
        RabbitUtil.releaseResource(connection, channel);
    }

}
