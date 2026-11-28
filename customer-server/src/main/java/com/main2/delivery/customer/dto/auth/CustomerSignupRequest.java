package com.main2.delivery.customer.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 고객 회원가입 요청. 형식 검사는 @Valid, 중복 검사는 서비스에서
// 비밀번호가 들어 있으므로 이 객체를 통째로 로그에 남기지 않는다
@Getter 
@Setter
@NoArgsConstructor 
public class CustomerSignupRequest {

    @NotBlank(message = "아이디는 필수입니다.")
    @Pattern(regexp = "[a-z0-9]{4,20}", message = "아이디는 영문 소문자,숫자 4~20자 입니다.")
    private String loginId;

    // 8~64자, 소문자·숫자·특수문자를 각각 하나 이상, 공백 없음 (대문자는 써도 되고 안 써도 됨)
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Pattern(regexp = "(?=.*[a-z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s])\\S{8,64}",
             message = "비밀번호는 8~64자, 영문 소문자·숫자·특수문자를 각각 하나 이상 넣어 주세요.")
    private String password;

    @NotBlank(message = "휴대전화 번호는 필수입니다.")
    private String phone;

    @NotBlank(message = "이름은 필수입니다.")
    @Size(max = 50, message = "이름은 50자 이하입니다.")
    private String name;

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(max=50, message = "닉네임은 50자 이하입니다.")
    private String nickname;
}
