package com.main2.delivery.common.security;

import org.springframework.http.HttpStatus;

import com.main2.delivery.common.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 회원가입·로그인 에러 코드 (고객·업주 서버가 같이 씀)
@Getter 
@RequiredArgsConstructor 
public enum AuthErrorCode implements ErrorCode {
    AUTH_LOGIN_ID_DUPLICAED(HttpStatus.CONFLICT, "이미 사용중인 아이디입니다."),
    AUTH_PHONE_DUPLICATED(HttpStatus.CONFLICT, "이미 가입된 휴대전화 번호입니다."),
    AUTH_NICKNAME_DUPLICATED(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    AUTH_INVALID_PHONE(HttpStatus.BAD_REQUEST, "휴대전화 번호는 01로 시작하는 숫자 10~11자리입니다."),
    AUTH_LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다.");   // 어느 쪽이 틀렸는지 알려 주지 않음

    private final HttpStatus status;
    private final String message;
}
