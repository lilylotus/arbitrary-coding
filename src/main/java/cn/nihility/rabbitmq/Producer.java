package cn.nihility.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 生产者，发送消息的对象
 */
public class Producer {

    private static final String QUEUE_NAME = "producer";

    /*
    * 此时运行代码，因为队列不存在，消息肯定没地方存储，
    * 但是程序却并未出错，也就是消息丢失了但是我们却并不知晓。
    * */

    public static void main(String[] args) throws IOException, TimeoutException {
        // 创建连接
        ConnectionFactory factory = new ConnectionFactory();
        // 设置 RabbitMQ 的主机名
        factory.setHost("tencent.nihility.cn");
        factory.setPort(50007);
        factory.setUsername("rabbit");
        factory.setPassword("rabbit");
        // 创建一个连接
        Connection connection = factory.newConnection();
        // 创建一个通道
        Channel channel = connection.createChannel();
        // 指定一个队列,不存在的话自动创建
        //channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        // 发送消息
        String message = "Hello World!";
        channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
        System.out.println(" [x] Sent '" + message + "'");
        // 关闭频道和连接
        channel.close();
        connection.close();
    }

}
