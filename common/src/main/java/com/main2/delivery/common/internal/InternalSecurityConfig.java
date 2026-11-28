package com.main2.delivery.common.internal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.main2.delivery.common.security.SecurityErrorResponder;

// 서버 간 호출(/internal/**) 전용 체인. 사용자 체인(SecurityConfig)보다 먼저 검사됨
// 보안 방식이 달라서(사용자 JWT가 아니라 공유 키) 체인을 나눔
@Configuration 
public class InternalSecurityConfig {

    @Bean 
    @Order(1)
    SecurityFilterChain internalFilterChain(
        HttpSecurity http,
        SecurityErrorResponder errorResponder,
        @Value("${internal.api-key:}") String apiKey) throws Exception {
            http.securityMatcher("/internal/**")
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())  // 통과 여부는 키 필터가 정함
                .addFilterBefore(
                    new InternalApiKeyFilter(apiKey, errorResponder), 
                    UsernamePasswordAuthenticationFilter.class
                );
            return  http.build();
        }

}
