package com.main2.delivery.common.internal;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.web.filter.OncePerRequestFilter;

import com.main2.delivery.common.exception.CommonErrorCode;
import com.main2.delivery.common.security.SecurityErrorResponder;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// /internal/** 전용: 서버끼리만 아는 공유 키가 맞는지 검사 (사용자 로그인과 무관)
// @Component를 붙이지 않는다 → 붙이면 톰캣 필터로도 등록돼 모든 요청에 실행됨. InternalSecurityConfig에서 new로 넣음

public class InternalApiKeyFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Internal-Api-Key";

    private final byte[] expectedKey;
    private final SecurityErrorResponder errorResponder;

    public InternalApiKeyFilter(String expectedKey, SecurityErrorResponder errorResponder) {
        this.expectedKey = (expectedKey == null) ? new byte[0] : expectedKey.getBytes(StandardCharsets.UTF_8);
        this.errorResponder = errorResponder;
    }

    @Override 
    protected void doFilterInternal( HttpServletRequest request, HttpServletResponse response, FilterChain chain )
        throws ServletException, IOException {
            if(!matches(request.getHeader(HEADER))){
                // 필터 오류는 GlobalExceptionHandler가 못 잡으므로 공통 응답을 직접 씀
                errorResponder.write(response, CommonErrorCode.COMMON_UNAUTHORIZED, "내부 API 키가 올바르지 않습니다.");
                return;
            }
            chain.doFilter(request, response);
        }
    
    // 키 설정이 비어 있으면 전부 거부(설정을 빼먹은 서버가 열려버리지 않게)
    // MessageDigest.isEqual: 앞 글자가 틀려도 끝까지 비교 → 응답 시간으로 키를 추측하지 못하게
    private boolean matches(String givenKey){
        if (expectedKey.length == 0 || givenKey == null ){
            return false;   
        }
        return MessageDigest.isEqual(expectedKey, givenKey.getBytes(StandardCharsets.UTF_8));
    }
}
