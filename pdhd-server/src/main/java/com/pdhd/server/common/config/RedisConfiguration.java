package com.pdhd.server.common.config;

import com.pdhd.server.dao.entity.User;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author wangsiqian
 */
@Configuration
@RequiredArgsConstructor
public class RedisConfiguration {
    private final RedisConnectionFactory factory;

    @Bean
    public RedisTemplate<String, User> userRedisTemplate() {
        return getRedisTemplate(User.class);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        return getRedisTemplate(Object.class);
    }

    private <T> RedisTemplate<String, T> getRedisTemplate(Class<T> type) {
        RedisTemplate<String, T> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);

        // 配置key序列化器
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());

        // 配置value序列化器
        RedisSerializer<T> customSerializer = getSerializer(type);
        redisTemplate.setValueSerializer(customSerializer);
        redisTemplate.setHashValueSerializer(customSerializer);
        return redisTemplate;
    }

    private <T> RedisSerializer<T> getSerializer(Class<T> type) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);

        Jackson2JsonRedisSerializer<T> serializer = new Jackson2JsonRedisSerializer<>(type);
        serializer.setObjectMapper(objectMapper);
        return serializer;
    }
}
