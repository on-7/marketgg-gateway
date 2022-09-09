package com.nhnacademy.marketgg.gateway.exception;

/**
 * 보안 인증서 관련 예외 클래스입니다.
 */
public class ClientHttpConnectionException extends RuntimeException {

    public ClientHttpConnectionException(Throwable cause) {
        super(cause);
    }

}
