package com.nhnacademy.marketgg.gateway.filter;

import com.nhnacademy.marketgg.gateway.jwt.JwtUtils;
import java.security.Key;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

/**
 * JWT 토큰을 이용한 인증을 해주는 필터입니다.
 */

@Slf4j
@Component
public class JwtAuthorizationFilter
    extends AbstractGatewayFilterFactory<JwtAuthorizationFilter.Config> {

    private static final int HEADER_BEARER = 7;

    private final Key key;
    private final String refreshRequestUrl;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 생성자입니다.
     *
     * @param jwtSecretUrl    - Secure Manager 에서 JWt Secret Key 를 요청하는 URL 입니다.
     * @param refreshTokenUrl - 만료된 토큰에 대해 갱신을 요청할 수 있는 URL 입니다.
     * @param redisTemplate   - 스프링 빈에 등록된 RedisTemplate 을 주입받습니다.
     */
    public JwtAuthorizationFilter(@Value("${jwt.secret-url}") String jwtSecretUrl,
                                  @Value("${jwt.refresh-request}") String refreshTokenUrl,
                                  RedisTemplate<String, Object> redisTemplate) {
        super(Config.class);
        this.key = JwtUtils.getKey(jwtSecretUrl);
        this.refreshRequestUrl = refreshTokenUrl;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            HttpHeaders headers = exchange.getRequest().getHeaders();

            if (!headers.containsKey(HttpHeaders.AUTHORIZATION)) {
                return chain.filter(exchange);
            }

            String authorizationHeader
                = Objects.requireNonNull(headers.get(HttpHeaders.AUTHORIZATION)).get(0);

            String jwt = authorizationHeader.substring(HEADER_BEARER);

            if (Objects.nonNull(redisTemplate.opsForValue().get(jwt))) {
                log.info("로그아웃 됨");
                return chain.filter(exchange);
            }

            Optional<String> token =
                Optional.ofNullable(JwtUtils.parseToken(jwt, key, refreshRequestUrl));

            if (token.isEmpty()) {
                return chain.filter(exchange);
            }

            HttpHeaders responseHeader = exchange.getResponse().getHeaders();

            responseHeader.setBearerAuth(token.get());
            responseHeader.set("USER-EMAIL", JwtUtils.getEmail(jwt, key));
            responseHeader.set(JwtUtils.AUTHORITIES, JwtUtils.getRoles(jwt, key));

            return chain.filter(exchange);
        };
    }

    /**
     * 설정 생성자.
     */
    public static class Config {

    }

}
