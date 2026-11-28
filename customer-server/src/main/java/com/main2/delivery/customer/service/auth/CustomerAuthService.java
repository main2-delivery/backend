package com.main2.delivery.customer.service.auth;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.main2.delivery.common.exception.BusinessException;
import com.main2.delivery.common.security.AuthErrorCode;
import com.main2.delivery.common.util.PhoneNumber;
import com.main2.delivery.core.entity.UserEntity;
import com.main2.delivery.customer.dto.auth.CustomerSignupRequest;
import com.main2.delivery.customer.dto.auth.CustomerSignupResponse;
import com.main2.delivery.customer.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class CustomerAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    @Transactional 
    public CustomerSignupResponse signUp(CustomerSignupRequest req){
        String phone = PhoneNumber.normalize(req.getPhone());
        String nickname = req.getNickname().trim();

        if(phone == null){
            throw new BusinessException(AuthErrorCode.AUTH_INVALID_PHONE);
        }
        if(userRepository.existsByLoginId(req.getLoginId())){
            throw new BusinessException(AuthErrorCode.AUTH_LOGIN_ID_DUPLICAED);
        }
        if(userRepository.existsByPhone(phone)){
            throw new BusinessException(AuthErrorCode.AUTH_PHONE_DUPLICATED);
        }
        if(userRepository.existsByNickname(nickname)){
            throw new BusinessException(AuthErrorCode.AUTH_NICKNAME_DUPLICATED);
        }

        UserEntity user = UserEntity.builder()
            .loginId(req.getLoginId())
            .phone(phone)
            .passwordHash(passwordEncoder.encode(req.getPassword())) // 원문 저장 X
            .name(req.getName().trim())
            .nickname(nickname)
            .build();
        try{
            userRepository.saveAndFlush(user);      // UNIQUE 위반을 여기서 바로 확인 (커밋 때까지 미루지 않음)
        }catch(DataIntegrityViolationException e){
            // 같은 아이디·번호·닉네임으로 동시에 가입: 위 검사를 모두 통과하고 DB UNIQUE에서 막힘
            throw new BusinessException(duplicateCode(e));
        }
        return new CustomerSignupResponse(user.getId());
    }

    private AuthErrorCode duplicateCode(DataIntegrityViolationException e){
        String message = String.valueOf(e.getMostSpecificCause().getMessage());

        if(message.contains("uk_user_phone")){
            return AuthErrorCode.AUTH_PHONE_DUPLICATED;
        }
        if(message.contains("uk_user_nickname")){
            return AuthErrorCode.AUTH_NICKNAME_DUPLICATED;
        }
        return AuthErrorCode.AUTH_LOGIN_ID_DUPLICAED;
    }
}
