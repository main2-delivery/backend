package com.main2.delivery.customer.config;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.main2.delivery.common.exception.BusinessException;
import com.main2.delivery.common.exception.CommonErrorCode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// /internal/** 은 서버끼리만. 헤더의 공유 키가 맞아야 통과
@Component 
public class InternalApiKeyInterceptor implements HandlerInterceptor{
    private final byte[] apiKey;

    public InternalApiKeyInterceptor(@Value("${internal.api-key}") String apiKey){
        this.apiKey = apiKey.getBytes(StandardCharsets.UTF_8);
    }

    @Override 
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler){
        String key = request.getHeader("X-Internal-Api-Key");
        // 문자열 equals 대신 MessageDigest.isEqual: 비교 시간이 일정해서 응답 시간으로 키를 추측할 수 없음
        if(key == null || !MessageDigest.isEqual(key.getBytes(StandardCharsets.UTF_8), apiKey)){
            throw new BusinessException(CommonErrorCode.COMMON_FORBIDDEN, "서버 간 호출 키가 올바르지 않습니다.");
        }
        return true;
    }
}
