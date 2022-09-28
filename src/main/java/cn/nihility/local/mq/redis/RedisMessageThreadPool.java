package cn.nihility.local.mq.redis;

import cn.nihility.local.mq.config.MessageReceiveProperties;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author yuanzx
 * @date 2022/09/28 11:22
 */
public class RedisMessageThreadPool {

    private final Object lock = new Object();

    private final Set<String> routingKeySet = new HashSet<>(16);

    private volatile ExecutorService executorService;

    public RedisMessageThreadPool() {
        // NO-OP
    }

    private ExecutorService executorService() {
        ExecutorService local = executorService;
        if (local == null) {
            synchronized (lock) {
                local = executorService;
                if (null == local) {
                    local = new ThreadPoolExecutor(10, 20, 0L, TimeUnit.SECONDS,
                            new LinkedBlockingQueue<>(1024), new RedisMessageThreadFactory());
                    executorService = local;
                }
            }
        }
        return local;
    }

    /**
     * 执行 redis 接收消息队列消息的任务
     *
     * @param routingKey    redis 获取队列消息的 key 值
     * @param receiveConfig 接收消息的配置
     * @param redisTemplate redis 操作工具类
     */
    public void startExecuteReceiveMessage(String routingKey, MessageReceiveProperties receiveConfig,
                                           RedisTemplate<String, Object> redisTemplate) {
        if (routingKeySet.contains(routingKey)) {
            return;
        }

        routingKeySet.add(routingKey);
        executorService().execute(new RedisScheduleReceiveMessageThread(routingKey, receiveConfig, redisTemplate));

    }

}
