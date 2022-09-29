package cn.nihility.local.mq.service.impl;

import cn.nihility.local.mq.config.MessageReceiveProperties;
import cn.nihility.local.mq.config.MessageSendProperties;
import cn.nihility.local.mq.dto.LocalMessageHolder;
import cn.nihility.local.mq.redis.RedisMessageThreadPool;
import cn.nihility.local.mq.service.IProxyMessageSendService;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Redis 来向消息队列发送消息
 *
 * @author yuanzx
 * @date 2022/09/28 10:30
 */
public class RedisMessageSendServiceImpl implements IProxyMessageSendService {

    private static final String SUPPORT_TYPE = "redis";

    private RedisTemplate<String, Object> redisTemplate;

    private final RedisMessageThreadPool threadPool = new RedisMessageThreadPool();

    private final Map<String, String> routingKeyMap = new ConcurrentHashMap<>(16);

    public RedisMessageSendServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void send(LocalMessageHolder message, MessageSendProperties senderConfig, MessageReceiveProperties receiveConfig) {

        String key = senderConfig.getRoutingKey();

        // 把消息实体添加到 list 头
        redisTemplate.opsForList().leftPush(key, message);

        if (!routingKeyMap.containsKey(key)) {
            threadPool.startExecuteReceiveMessage(key, receiveConfig, redisTemplate);
            routingKeyMap.put(key, key);
        }

    }

    @Override
    public boolean support(String type) {
        return SUPPORT_TYPE.equals(type);
    }

}
