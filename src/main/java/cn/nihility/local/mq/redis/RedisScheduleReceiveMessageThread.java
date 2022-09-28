package cn.nihility.local.mq.redis;

import cn.nihility.local.mq.config.MessageReceiveProperties;
import cn.nihility.local.mq.disruptor.DisruptorHandlerProxyInvoke;
import cn.nihility.local.mq.dto.LocalMessageHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;

/**
 * redis 定时拉取指定 routingKey list 中的消息数据并消息
 *
 * @author yuanzx
 * @date 2022/09/28 11:08
 */
public class RedisScheduleReceiveMessageThread implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(RedisScheduleReceiveMessageThread.class);

    private static final int DEFAULT_SLEEP_MILLI = 200;

    private static final int STOP_SLEEP_MILLI = 2000;

    private final String routingKey;

    private MessageReceiveProperties receiverConfig;

    private RedisTemplate<String, Object> redisTemplate;

    public RedisScheduleReceiveMessageThread(String routingKey, MessageReceiveProperties receiveConfig,
                                             RedisTemplate<String, Object> redisTemplate) {
        this.routingKey = routingKey;
        this.receiverConfig = receiveConfig;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void run() {
        int sleepDuration = DEFAULT_SLEEP_MILLI;
        while (!Thread.interrupted()) {
            // 获取 list 头的数据
            LocalMessageHolder message =
                    (LocalMessageHolder) redisTemplate.opsForList().leftPop(routingKey, Duration.ofMillis(100));
            if (null != message) {
                try {
                    DisruptorHandlerProxyInvoke.invokeProxyMethod(receiverConfig, message);
                } catch (Exception ex) {
                    // NO-OP
                    log.error("Redis 处理消息队列 [" + routingKey + "] 消息异常", ex);
                }
                sleepDuration = DEFAULT_SLEEP_MILLI;
            } else {
                // 每次没有拿到消息，停顿时间加 200 毫秒
                if (sleepDuration < STOP_SLEEP_MILLI) {
                    sleepDuration += DEFAULT_SLEEP_MILLI;
                }
                try {
                    Thread.sleep(sleepDuration);
                } catch (InterruptedException ignore) {
                    // NO-OP
                }
            }
        }

    }

}
