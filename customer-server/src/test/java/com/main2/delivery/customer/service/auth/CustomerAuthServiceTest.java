package com.main2.delivery.customer.service.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.main2.delivery.common.exception.BusinessException;
import com.main2.delivery.common.security.AuthErrorCode;
import com.main2.delivery.core.entity.UserEntity;
import com.main2.delivery.customer.dto.auth.CustomerSignupRequest;
import com.main2.delivery.customer.repository.user.UserRepository;

// DB 없이 돌리는 테스트 (리포지토리는 가짜, 암호화는 진짜 BCrypt)
@ExtendWith(MockitoExtension.class)
class CustomerAuthServiceTest {

    @Mock
    UserRepository userRepository;

    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    CustomerAuthService service;

    @BeforeEach
    void setUp() {
        service = new CustomerAuthService(userRepository, passwordEncoder);
    }

    private CustomerSignupRequest request(String loginId, String phone, String nickname) {
        CustomerSignupRequest req = new CustomerSignupRequest();
        req.setLoginId(loginId);
        req.setPassword("panda123!");
        req.setPhone(phone);
        req.setName("홍길동");
        req.setNickname(nickname);
        return req;
    }

    @Test
    @DisplayName("가입하면 비밀번호는 BCrypt로, 번호는 숫자만, 닉네임은 앞뒤 공백을 빼고 저장된다")
    void signUp_hashesPasswordAndNormalizes() {
        service.signUp(request("hungrypanda", "010-1234-5678", "  배고픈판다 "));

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).saveAndFlush(captor.capture());
        UserEntity saved = captor.getValue();
        assertThat(saved.getPhone()).isEqualTo("01012345678");
        assertThat(saved.getNickname()).isEqualTo("배고픈판다");
        assertThat(saved.getPasswordHash()).isNotEqualTo("panda123!");
        assertThat(passwordEncoder.matches("panda123!", saved.getPasswordHash())).isTrue();
    }

    @Test
    @DisplayName("이미 있는 아이디면 409 AUTH_LOGIN_ID_DUPLICATED, 저장하지 않는다")
    void signUp_duplicateLoginId() {
        given(userRepository.existsByLoginId("hungrypanda")).willReturn(true);

        assertThatThrownBy(() -> service.signUp(request("hungrypanda", "01012345678", "배고픈판다")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.AUTH_LOGIN_ID_DUPLICATED);
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("이미 가입된 번호면 409 AUTH_PHONE_DUPLICATED (하이픈이 있어도 같은 번호)")
    void signUp_duplicatePhone() {
        given(userRepository.existsByPhone("01012345678")).willReturn(true);

        assertThatThrownBy(() -> service.signUp(request("newpanda", "010-1234-5678", "배고픈판다")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.AUTH_PHONE_DUPLICATED);
    }

    @Test
    @DisplayName("이미 쓰는 닉네임이면 409 AUTH_NICKNAME_DUPLICATED, 저장하지 않는다")
    void signUp_duplicateNickname() {
        given(userRepository.existsByNickname("배고픈판다")).willReturn(true);

        assertThatThrownBy(() -> service.signUp(request("newpanda", "01099998888", " 배고픈판다 ")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.AUTH_NICKNAME_DUPLICATED);
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("휴대전화 형식이 아니면 400 AUTH_INVALID_PHONE")
    void signUp_invalidPhone() {
        assertThatThrownBy(() -> service.signUp(request("hungrypanda", "02-123-4567", "배고픈판다")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.AUTH_INVALID_PHONE);
    }

    @Test
    @DisplayName("동시에 같은 번호로 가입해 DB UNIQUE에 막히면 409 AUTH_PHONE_DUPLICATED")
    void signUp_concurrentDuplicatePhone() {
        given(userRepository.saveAndFlush(any()))
                .willThrow(new DataIntegrityViolationException(
                        "Duplicate entry '01012345678' for key 'user.uk_user_phone'"));

        assertThatThrownBy(() -> service.signUp(request("hungrypanda", "01012345678", "배고픈판다")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.AUTH_PHONE_DUPLICATED);
    }

    @Test
    @DisplayName("동시에 같은 닉네임으로 가입해 DB UNIQUE에 막히면 409 AUTH_NICKNAME_DUPLICATED")
    void signUp_concurrentDuplicateNickname() {
        given(userRepository.saveAndFlush(any()))
                .willThrow(
                        new DataIntegrityViolationException("Duplicate entry '배고픈판다' for key 'user.uk_user_nickname'"));

        assertThatThrownBy(() -> service.signUp(request("hungrypanda", "01012345678", "배고픈판다")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.AUTH_NICKNAME_DUPLICATED);
    }
}