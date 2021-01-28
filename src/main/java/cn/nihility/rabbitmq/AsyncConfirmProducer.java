package cn.nihility.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeoutException;

public class AsyncConfirmProducer {

    private static final String EXCHANGE_NAME = "async-confirm-exchange";

    /*
    * 事务机制最慢，普通 confirm 机制虽有提升但是不多，批量 confirm 和异步 confirm 性能最好，建议使用异步 confirm 机制。
    *
    * */

    public static void main(String[] args) throws IOException, TimeoutException {
        // 创建一个连接
        Connection connection = RabbitUtil.obtainConnection();
        // 创建一个通道
        Channel channel = connection.createChannel();
        // 创建一个Exchange
        channel.exchangeDeclare(EXCHANGE_NAME, "direct");
        channel.queueDeclare(EXCHANGE_NAME, false, false, false, null);

        int batchCount = 100;
        long msgCount = 1;
        final SortedSet<Long> confirmSet = new TreeSet<>();

        channel.confirmSelect();

        channel.addConfirmListener(new ConfirmListener() {
            @Override
            public void handleAck(long deliveryTag, boolean multiple) throws IOException {
                System.out.println("Ack,SeqNo：" + deliveryTag + ",multiple：" + multiple);
                if (multiple) {
                    confirmSet.headSet(deliveryTag - 1).clear();
                } else {
                    confirmSet.remove(deliveryTag);
                }
            }

            @Override
            public void handleNack(long deliveryTag, boolean multiple) throws IOException {
                System.out.println("Nack,SeqNo：" + deliveryTag + ",multiple：" + multiple);
                if (multiple) {
                    confirmSet.headSet(deliveryTag - 1).clear();
                } else {
                    confirmSet.remove(deliveryTag);
                }
                // 注意这里需要添加处理消息重发的场景
            }
        });
        // 演示发送100个消息
        while (msgCount <= batchCount) {
            long nextSeqNo = channel.getNextPublishSeqNo();
            channel.basicPublish(EXCHANGE_NAME, "", null, "async confirm test".getBytes());
            confirmSet.add(nextSeqNo);
            msgCount = nextSeqNo;
        }

        try {
            Thread.sleep(5000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 关闭频道和连接
        RabbitUtil.releaseResource(connection, channel);
    }

}
