package cn.nihility.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitUtil {

    public static ConnectionFactory createConnectionFactory() {
        // 创建连接
        ConnectionFactory factory = new ConnectionFactory();
        // 设置 RabbitMQ 的主机名
        factory.setHost("tencent.nihility.cn");
        factory.setPort(50007);
        factory.setUsername("rabbit");
        factory.setPassword("rabbit");
        return factory;
    }

    public static Connection obtainConnection() throws IOException, TimeoutException {
        ConnectionFactory factory = createConnectionFactory();
        return factory.newConnection();
    }

    public static void releaseResource(Connection connection, Channel channel) {
        if (null != channel) {
            try {
                channel.close();
            } catch (IOException | TimeoutException e) {
                e.printStackTrace();
            }
        }
        if (null != connection) {
            try {
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
