package com.nhnacademy.marketgg.gateway.config;

import com.nhnacademy.exception.SecureManagerException;
import com.nhnacademy.marketgg.gateway.secure.SecureUtils;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Redis 설정을 담당합니다.
 */
@Slf4j
@Configuration
public class RedisConfig {

    private static final int REDIS_DURATION_SECOND = 5;

    private final String host;
    private final int port;
    private final int database;
    private final String password;

    public RedisConfig(final @Value("${gg.redis.password-url}") String redisPasswordUrl,
                       final @Value("${gg.redis.url}") String redisInfoUrl,
                       SecureUtils secureUtils) {
        ClientHttpConnector clientHttpConnector = secureUtils.getClientHttpConnector();
        String[] info = this.getRedisInfo(redisInfoUrl, clientHttpConnector);
        this.host = info[0];
        this.port = Integer.parseInt(info[1]);
        this.database = Integer.parseInt(info[2]);
        this.password = this.getRedisPassword(redisPasswordUrl, clientHttpConnector);
    }

    /**
     * Redis 연결과 관련된 설정을 하는 RedisConnectionFactory 를 스프링 빈으로 등록한다.
     *
     * @return RedisConnectionFactory
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();

        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setPassword(password);
        configuration.setDatabase(database);

        return new LettuceConnectionFactory(configuration);
    }

    /**
     * RedisTemplate 을 스프링 빈 등록합니다.
     *
     * @param redisConnectionFactory - 스프링 빈으로 등록된 RedisConnectionFactory
     * @return RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
        RedisConnectionFactory redisConnectionFactory) {

        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(String.class));

        return redisTemplate;
    }

    private String[] getRedisInfo(String infoUrl, ClientHttpConnector clientHttpConnector) {
        Map<String, Map<String, String>> block
            = WebClient.builder()
                       .clientConnector(clientHttpConnector)
                       .build()
                       .get()
                       .uri(infoUrl)
                       .retrieve()
                       .bodyToMono(
                           new ParameterizedTypeReference<Map<String, Map<String, String>>>() {
                           })
                       .timeout(Duration.ofSeconds(
                           REDIS_DURATION_SECOND))
                       .block();

        String connectInfo = Optional.ofNullable(block)
                                     .orElseThrow(IllegalArgumentException::new)
                                     .get("body")
                                     .get("secret");

        String[] info = connectInfo.split(":");

        if (info.length != 3) {
            throw new SecureManagerException();
        }

        return info;
    }

    private String getRedisPassword(String passwordUrl, ClientHttpConnector clientHttpConnector) {
        Map<String, Map<String, String>> block
            = WebClient.builder()
                       .clientConnector(clientHttpConnector)
                       .build()
                       .get()
                       .uri(passwordUrl)
                       .retrieve()
                       .bodyToMono(
                           new ParameterizedTypeReference<Map<String, Map<String, String>>>() {
                           })
                       .timeout(Duration.ofSeconds(
                           REDIS_DURATION_SECOND))
                       .block();

        return Optional.ofNullable(block)
                       .orElseThrow(IllegalArgumentException::new)
                       .get("body")
                       .get("secret");
    }

}
