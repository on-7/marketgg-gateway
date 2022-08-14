package com.nhnacademy.marketgg.gateway.filter;

import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HeaderFilter extends AbstractGatewayFilterFactory<HeaderFilter.Config> {

    public HeaderFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {

            ServerHttpRequest request = exchange.getRequest();
            HttpHeaders headers = request.getHeaders();

            List<String> forwardedFor = headers.get(config.getForwardedFor());

            if (Objects.nonNull(forwardedFor) && Objects.nonNull(forwardedFor.get(0))) {
                log.info("{}: {}", config.getForwardedFor(), forwardedFor.get(0));
            }


            return chain.filter(exchange);
        });
    }

    /**
     * 설정 생성자.
     */
    @AllArgsConstructor
    @Getter
    public static class Config {
        private final String forwardedFor;
    }

}
