package cn.nihility.local.mq.config;

import cn.nihility.local.mq.service.IProxyMessageSendService;
import cn.nihility.local.mq.service.impl.DisruptorMessageSendServiceImpl;
import cn.nihility.local.mq.service.impl.RedisMessageSendServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

/**
 * 本地 mq （消息队列）配置类
 *
 * @author yuanzx
 * @date 2022/09/27 11:10
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MessageConfigurationProperties.class)
@ConditionalOnProperty(prefix = MessageConfigurationProperties.PREFIX, name = "enable")
public class LocalMessageQueueConfiguration {

    @Bean
    public IProxyMessageSendService disruptorMessageSendService() {
        return new DisruptorMessageSendServiceImpl();
    }

    @Bean
    public IProxyMessageSendService redisMessageSendService(RedisTemplate<String, Object> redisTemplate) {
        return new RedisMessageSendServiceImpl(redisTemplate);
    }

    @Bean
    public LocalMessagePostProcessor localMessagePostProcessor(MessageConfigurationProperties properties,
                                                               List<IProxyMessageSendService> messageSendServiceList) {
        return new LocalMessagePostProcessor(properties, messageSendServiceList);
    }

}
