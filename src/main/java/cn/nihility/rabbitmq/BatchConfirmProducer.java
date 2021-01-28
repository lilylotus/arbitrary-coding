package cn.nihility.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

/**
 * 批量 confirm
 */
public class BatchConfirmProducer {

    private static final String EXCHANGE_NAME = "batch-confirm-exchange";

    public static void main(String[] args) throws IOException, TimeoutException {
        // 创建一个连接
        Connection connection = RabbitUtil.obtainConnection();
        // 创建一个通道
        Channel channel = connection.createChannel();
        // 创建一个Exchange
        channel.exchangeDeclare(EXCHANGE_NAME, "direct");

        int batchCount = 100;
        int msgCount = 0;
        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<>(batchCount);
        try {
            channel.confirmSelect();

            String message = "batch confirm test";

            while (msgCount <= batchCount) {
                channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes());
                // 将发送出去的消息存入缓存中，缓存可以是一个 ArrayList 或者 BlockingQueue 之类的
                blockingQueue.add(message);

                if (++msgCount >= batchCount) {
                    try {
                        if (channel.waitForConfirms()) {
                            System.out.println("批量发送成功");
                            // 将缓存中的消息清空
                            blockingQueue.clear();
                        } else {
                            // 将缓存中的消息重新发送
                            System.out.println("批量发送失败");
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        // 将缓存中的消息重新发送
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 关闭频道和连接
        RabbitUtil.releaseResource(connection, channel);
    }

}
