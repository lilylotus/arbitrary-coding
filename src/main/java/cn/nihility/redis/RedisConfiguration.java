package cn.nihility.redis;

import cn.nihility.redis.listener.RedisKeyExpirationListener;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 的配置类
 */
@Configuration
public class RedisConfiguration extends CachingConfigurerSupport {

    private static final Logger log = LoggerFactory.getLogger(RedisConfiguration.class);

    /**
     * 自定义缓存 key 的生成策略，默认的生成策略是看不懂的(乱码内容)
     * 通过 Spring 的依赖注入特性进行自定义的配置注入并且此类是一个配置类可以更多程度的自定义配置
     */
    @Override
    public KeyGenerator keyGenerator() {
        log.info("RedisConfiguration : configuration keyGenerator");
        return (target, method, params) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(target.getClass().getName()).append(":");
            sb.append(method.getName()).append(":");
            for (Object obj : params) {
                sb.append(obj.toString()).append(":");
            }
            return sb.toString();
        };
    }

    /**
     * 缓存配置管理器
     */
    @Bean
    public CacheManager cacheManager(LettuceConnectionFactory factory) {
        log.info("RedisConfiguration : Generate CacheManager");
        // 以锁写入的方式创建 RedisCacheWriter 对象
        RedisCacheWriter writer = RedisCacheWriter.lockingRedisCacheWriter(factory);
        // 创建默认缓存配置对象
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
        return new RedisCacheManager(writer, config);
    }

    @Bean
    @ConditionalOnMissingBean(name = "redisTemplate")
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory connectionFactory) {
        log.info("RedisConfiguration : Generate RedisTemplate");
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);

        final ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.activateDefaultTyping(om.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL);

        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        jackson2JsonRedisSerializer.setObjectMapper(om);

        redisTemplate.setKeySerializer(StringRedisSerializer.UTF_8);
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashKeySerializer(StringRedisSerializer.UTF_8);
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);

        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }

    /* ============================== Redis Key 过期监控 ============================== */

    @Bean
    public RedisMessageListenerContainer listenerContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer listenerContainer = new RedisMessageListenerContainer();
        listenerContainer.setConnectionFactory(connectionFactory);
        return listenerContainer;
    }

    @Bean
    public KeyExpirationEventMessageListener redisKeyExpirationListener(RedisMessageListenerContainer listenerContainer) {
        return new RedisKeyExpirationListener(listenerContainer);
    }

}
