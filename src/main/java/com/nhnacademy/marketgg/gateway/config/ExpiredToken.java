package com.nhnacademy.marketgg.gateway.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 만료 전이지만 더 이상 사용하지 않는 토큰을 관리합니다.
 */

@AllArgsConstructor
@Getter
public class ExpiredToken {

    private final String email;
    private final String jwt;

}
