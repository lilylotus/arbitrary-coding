package cn.nihility.redis.service.impl;

import cn.nihility.redis.service.PublishService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class PublishServiceImpl implements PublishService {
    private final static Logger log = LoggerFactory.getLogger(PublishServiceImpl.class);

    private final StringRedisTemplate stringRedisTemplate;

    public PublishServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 订阅主题
     *
     * @param topicName 发布的管道
     * @param message   发布的内容
     */
    @Override
    public void publish(String topicName, String message) {
        stringRedisTemplate.convertAndSend(topicName, message);
    }

}
