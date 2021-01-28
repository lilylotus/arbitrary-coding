package cn.nihility.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * 普通的 confirm 机制
 */
public class NormalConfirmProducer {

    private static final String EXCHANGE_NAME = "normal-confirm-exchange";

    /*
    * channel.confirmSelect();
    * 将信道设置成 confirm 模式。
    *
    * channel.waitForConfirms();
    * 等待发送消息的确认消息，如果发送成功，则返回 true，如果发送失败，则返回 false。
    *
    * 如果不开启信道的 confirm 模式，调用 channel.waitForConfirms() 会报错
    *
    * 注意：事务机制和 publisher confirm 机制是互斥的，不能共存。
    * */

    public static void main(String[] args) throws IOException, TimeoutException {
        // 创建一个连接
        Connection connection = RabbitUtil.obtainConnection();
        // 创建一个通道
        Channel channel = connection.createChannel();
        // 创建一个Exchange
        channel.exchangeDeclare(EXCHANGE_NAME, "direct");

        sendConfirmSingleMsg(channel, "Single Confirm Message");

        List<String> msgList = new ArrayList<>();
        msgList.add("Confirm batch message 1");
        msgList.add("Confirm batch message 2");
        msgList.add("Confirm batch message 3");
        msgList.add("Confirm batch message 4");
        msgList.add("Confirm batch message 5");
        confirmSendBatchMsg(channel, msgList);

        // 关闭频道和连接
        RabbitUtil.releaseResource(connection, channel);

    }

    public static void sendConfirmSingleMsg(Channel channel, String message) {
        try {
            channel.confirmSelect();
            // 发送消息

            channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes());

            if (channel.waitForConfirms()) {
                System.out.println("send a message [" + message + "] success");
            } else {
                System.out.println("send a message [" + message + "]failed");
                // do something else...
            }

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void confirmSendBatchMsg(Channel channel, List<String> msgList) throws IOException {
        channel.confirmSelect();

        for (String message : msgList) {
            try {
                channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes());

                if (channel.waitForConfirms()) {
                    System.out.println("send a message [" + message + "] success");
                } else {
                    System.out.println("send a message [" + message + "]failed");
                    // do something else...
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

}
