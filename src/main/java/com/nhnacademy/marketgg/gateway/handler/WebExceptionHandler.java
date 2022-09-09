package com.nhnacademy.marketgg.gateway.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.marketgg.gateway.entity.ErrorEntity;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Gateway 에서 발생한 예외를 처리합니다.
 */
@Slf4j
@Order(-1)
@Component
@RequiredArgsConstructor
public class WebExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.error("", ex);
        ErrorEntity error = new ErrorEntity(ex.getMessage());

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(error);
        } catch (JsonProcessingException e) {
            bytes = "Error".getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);

        return exchange.getResponse()
                       .writeWith(Flux.just(buffer));
    }

}
