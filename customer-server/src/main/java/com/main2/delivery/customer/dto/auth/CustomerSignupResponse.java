package com.main2.delivery.customer.dto.auth;

import lombok.Getter;

// 가입 결과는 번호만 (비밀번호·해시는 절대 응답에 넣지 않음)
@Getter
public class CustomerSignupResponse {

    private final Long userId;

    public CustomerSignupResponse(Long userId){
        this.userId = userId;
    }
}
