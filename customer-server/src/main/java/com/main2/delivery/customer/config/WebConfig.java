package com.main2.delivery.customer.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// /internal/** 키 검사는 common InternalApiKeyFilter가 함 (서버별 인터셉터 없음)
@Configuration
public class WebConfig implements WebMvcConfigurer {

    // Flutter 웹(크롬, localhost:랜덤포트)에서 고객 API를 부를 수 있게 허용
    // /internal/** 은 서버끼리만 부르므로 열지 않음
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}