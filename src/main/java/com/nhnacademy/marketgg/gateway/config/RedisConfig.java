package com.nhnacademy.marketgg.gateway.config;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Redis 설정을 담당합니다.
 */

@Configuration
public class RedisConfig {

    @Value("${redis.host}")
    private String host;

    @Value("${redis.port}")
    private int port;

    @Value("${redis.database}")
    private int database;

    private final String password;

    public RedisConfig(@Value("${redis.password-url}") String redisPasswordUrl) {
        this.password = getRedisPassword(redisPasswordUrl);
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();

        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setPassword(password);
        configuration.setDatabase(database);

        return new LettuceConnectionFactory(configuration);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(ExpiredToken.class));

        return redisTemplate;
    }

    private String getRedisPassword(String passwordUrl) {
        Map<String, Map<String, String>> block = WebClient.create()
                                                          .get()
                                                          .uri(passwordUrl)
                                                          .retrieve()
                                                          .bodyToMono(Map.class)
                                                          .timeout(Duration.ofSeconds(3))
                                                          .block();

        return Optional.ofNullable(block)
                       .orElseThrow(IllegalArgumentException::new)
                       .get("body")
                       .get("secret");
    }

}
