package com.nhnacademy.marketgg.gateway.filter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * JWT 토큰을 이용한 인증을 해주는 필터입니다.
 */

@Slf4j
@Component
public class JwtAuthorizationFilter
    extends AbstractGatewayFilterFactory<JwtAuthorizationFilter.Config> {

    private static final int HEADER_BEARER = 7;

    private final String secret;
    private final Key key;
    private final String refreshRequestUrl;

    /**
     * 생성자.
     *
     * @param jwtSecretUrl    NHN Secure Manager 에서 JWt Secret Key 를 요청하는 URL 입니다.
     * @param refreshTokenUrl 만료된 토큰에 대해 갱신을 요청할 수 있는 URL 입니다.
     */
    public JwtAuthorizationFilter(@Value("${jwt.secret-url}") String jwtSecretUrl,
                                  @Value("${jwt.refresh-request}") String refreshTokenUrl) {
        super(Config.class);
        this.secret = jwtSecret(jwtSecretUrl);
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(secret));
        this.refreshRequestUrl = refreshTokenUrl;
    }

    @Override
    public GatewayFilter apply(Config config) {
        log.info("filtering");
        log.info(secret);
        return (exchange, chain) -> {
            HttpHeaders headers = exchange.getRequest().getHeaders();

            if (!headers.containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "토큰이 존재하지 않습니다.");
            }

            String authorizationHeader
                = Objects.requireNonNull(headers.get(HttpHeaders.AUTHORIZATION)).get(0);

            String jwt = authorizationHeader.substring(HEADER_BEARER);

            Optional<String> token = Optional.ofNullable(parseToken(jwt));

            if (token.isEmpty()) {
                return onError(exchange, "유효하지 않은 토큰입니다.");
            }

            exchange.getResponse().getHeaders().setBearerAuth(token.get());
            return chain.filter(exchange);
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String msg) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);

        log.error(msg);

        return response.setComplete();
    }

    private String parseToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);

            return token;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.error("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다.");
            return requestRenewToken(token);
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰이 잘못되었습니다.");
        }
        return null;
    }

    /**
     * 새로운 토큰을 요청합니다.
     *
     * @param jwt 만료된 JWT 토큰입니다.
     * @return 갱신된 JWT 토큰입니다.
     */
    private String requestRenewToken(String jwt) {
        return Optional.ofNullable(
                           Objects.requireNonNull(
                                      WebClient.create(refreshRequestUrl)
                                               .get()
                                               .headers(httpHeaders ->
                                                   httpHeaders.setBearerAuth(jwt))
                                               .exchangeToMono(r -> r.toEntity(Void.class))
                                               .block())
                                  .getHeaders()
                                  .get(HttpHeaders.AUTHORIZATION))
                       .map(h -> h.get(0).substring(7))
                       .orElse(null);
    }

    /**
     * 설정 생성자.
     */
    public static class Config {

    }

    /**
     * NHN Secure Manager 로 부터 JWT Secret 을 응답받습니다.
     *
     * @param jwtSecretUrl NHN Secure Manager 요청 주소.
     * @return JWT Secret.
     */
    public String jwtSecret(String jwtSecretUrl) {
        Map<String, Map<String, String>> block = WebClient.create()
                                                          .get()
                                                          .uri(jwtSecretUrl)
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
