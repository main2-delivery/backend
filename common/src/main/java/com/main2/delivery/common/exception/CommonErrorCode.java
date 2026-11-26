package com.main2.delivery.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter 
@RequiredArgsConstructor 
public enum CommonErrorCode implements ErrorCode {
    
    COMMON_INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    COMMON_INVALID_JSON(HttpStatus.BAD_REQUEST, "요청 본문을 읽을 수 없습니다."),
    COMMON_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    COMMON_FORBBIDEN(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    COMMON_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 주소를 찾을 수 없습니다."),
    COMMON_METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 요청 방식입니다."),
    COMMON_CONFLICT(HttpStatus.CONFLICT, "요청이 현재 상태와 충돌합니다."),
    COMMON_INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;
    
}
