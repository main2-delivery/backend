package com.main2.delivery.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// 가입할 때 암호화한 방식과 로그인할 때 비교하는 방식이 같아야 함 → 이 빈 하나를 모두가 씀
@Configuration 
public class PasswordConfig {

    @Bean 
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
