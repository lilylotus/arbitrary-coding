package cn.nihility.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 基于事务的生产者，发送消息保证消息确实被送达
 */
public class TransactionProducer {

    private static final String QUEUE_NAME = "txQueue";

    /*
    * 虽然事务能够解决消息发送方和 RabbitMQ 之间消息确认的问题
    * 只有消息成功被 RabbitMQ 接收，事务才能提交成功，否则便可在捕获异常之后进行事务回滚。
    * 但是使用事务机制会“吸干” RabbitMQ 的性能，因此建议使用下面讲到的发送方确认机制。
    * */

    public static void main(String[] args) throws IOException, TimeoutException {
        // 创建一个连接
        Connection connection = RabbitUtil.obtainConnection();
        // 创建一个通道
        Channel channel = connection.createChannel();
        // 指定一个队列,不存在的话自动创建
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        for (int i = 0; i < 10; i++) {
            sendMsg(channel, "Send Tx Message [" + i + "]", false);
        }

        // 关闭频道和连接
        RabbitUtil.releaseResource(connection, channel);
    }

    public static void sendMsg(Channel channel, String message, boolean haveError) {
        try {

            // 开启事务
            channel.txSelect();

            // 发送消息
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());

            if (haveError) {
                // 触发异常
                System.out.println(1 / 0);
            }

            // 提交
            channel.txCommit();

            System.out.println(" [x] Sent '" + message + "'");
        } catch (Exception ex) {
            ex.printStackTrace();

            // 回滚数据
            try {
                channel.txRollback();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
