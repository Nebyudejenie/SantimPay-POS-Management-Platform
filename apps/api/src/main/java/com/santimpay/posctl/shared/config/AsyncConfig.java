package com.santimpay.posctl.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Shared infrastructure beans. {@link StringRedisTemplate} backs the cache and the outbox relay's
 * Redis Streams publisher.
 */
@Configuration
public class AsyncConfig {

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory cf) {
        return new StringRedisTemplate(cf);
    }
}
