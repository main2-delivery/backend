package com.main2.delivery.customer.controller.auth;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.main2.delivery.common.response.ApiResponse;
import com.main2.delivery.customer.dto.auth.CustomerSignupRequest;
import com.main2.delivery.customer.dto.auth.CustomerSignupResponse;
import com.main2.delivery.customer.service.auth.CustomerAuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// 고객 회원가입·로그인 (로그인·내 정보는 다음 단계)
@RestController
@RequestMapping("/api/customer/auth")
@RequiredArgsConstructor
public class CustomerAuthController {

    private final CustomerAuthService customerAuthService;

    // 회원가입
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CustomerSignupResponse> signUp(@Valid @RequestBody CustomerSignupRequest req){
        return ApiResponse.ok(customerAuthService.signUp(req));
    }
}