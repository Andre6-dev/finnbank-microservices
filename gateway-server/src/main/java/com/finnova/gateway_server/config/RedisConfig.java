package com.finnova.gateway_server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis Configuration.
 * Configures reactive Redis connection for caching and session management.
 *
 * @author Andre Gallegos
 * @version 1.0.0
 */
@Configuration
public class RedisConfig {

//    @Value("${spring.data.redis.host}")
//    private String redisHost;
//
//    @Value("${spring.data.redis.port}")
//    private int redisPort;
//
//    @Value("${spring.data.redis.password}")
//    private String redisPassword;
//
//    /**
//     * Creates a reactive Redis connection factory.
//     *
//     * @return the reactive Redis connection factory
//     */
//    @Bean
//    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
//        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
//        configuration.setHostName(redisHost);
//        configuration.setPort(redisPort);
//        configuration.setPassword(RedisPassword.of(redisPassword));
//
//        return new LettuceConnectionFactory(configuration);
//    }

    /**
     * Creates a reactive Redis template for String operations.
     *
     * @param factory the reactive Redis connection factory
     * @return the reactive Redis template
     */
    @Bean
    public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory factory) {

        StringRedisSerializer keySerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<String> valueSerializer =
                new Jackson2JsonRedisSerializer<>(String.class);

        RedisSerializationContext.RedisSerializationContextBuilder<String, String> builder =
                RedisSerializationContext.newSerializationContext(keySerializer);

        RedisSerializationContext<String, String> context = builder
                .value(valueSerializer)
                .hashKey(keySerializer)
                .hashValue(valueSerializer)
                .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }
}
