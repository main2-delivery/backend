package com.main2.delivery.common.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import tools.jackson.databind.json.JsonMapper;

// 서버 4개 공통 보안 설정. Sprint 1 임시: 모든 요청 허용 + 개발용 헤더로 사용자 주입
// #32에서 URL별 권한, JWT 쿠키 필터, 401·403 처리기로 바뀜

@Configuration 
public class SecurityConfig {

    @Bean 
    SecurityErrorResponder securityErrorResponder(JsonMapper jsonMapper) {
        return new SecurityErrorResponder(jsonMapper);
    }

    @Bean 
    SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        SecurityErrorResponder errorResponder,
        @Value ("${ddip.auth.dev-header.enabled:false}") boolean devHeaderEnabled)
            throws Exception {
                http.csrf(csrf -> csrf.disable())           // 쿠키 SameSite로 대응 예정, 지금은 끔
                    .formLogin(form -> form.disable())      // 로그인 화면 안 씀 (API 서버)
                    .httpBasic(basic -> basic.disable())
                    .logout(logout -> logout.disable())     // 로그아웃은 우리 API로 (# 32)
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth ->  auth.anyRequest().permitAll()); // 임시: 전부 허용
                
                if ( devHeaderEnabled){
                    http.addFilterBefore(
                        new DevHeaderAuthenticationFilter(errorResponder), 
                        UsernamePasswordAuthenticationFilter.class
                    );
                }
                return  http.build();
            }
}
