package com.nhnacademy.marketgg.gateway.filter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class JwtAuthorizationFilter
    extends AbstractGatewayFilterFactory<JwtAuthorizationFilter.Config> {

    private final Key key;
    private final String refreshRequestUrl;

    public JwtAuthorizationFilter(@Value("${jwt.secret}") String secret,
                                  @Value("${jwt.refresh-request}") String url) {
        super(Config.class);
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(secret));
        this.refreshRequestUrl = url;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            HttpHeaders headers = exchange.getRequest().getHeaders();

            if (!headers.containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "토큰이 존재하지 않습니다.");
            }

            String authorizationHeader =
                Objects.requireNonNull(headers.get(HttpHeaders.AUTHORIZATION)).get(0);

            String jwt = authorizationHeader.substring(7);

            // FIXME: 기간이 만료된 JWT 라면 인증 서버에 Refresh Token 사용하는 요청을 보내야한다.
            if (Objects.isNull(parseToken(jwt))) {
                return onError(exchange, "유효하지 않은 토큰입니다.");
            }

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
            // 토큰을 파싱해보고 발생하는 exception catch, 문제가 생기면 false, 정상이면 true return
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);

            return token;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.error("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰이 잘못되었습니다.");
        }
        return null;
    }

    public static class Config {

    }
}
