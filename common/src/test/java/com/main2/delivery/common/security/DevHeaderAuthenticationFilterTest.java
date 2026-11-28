package com.main2.delivery.common.security;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.main2.delivery.common.response.ApiResponse;

// 개발용 헤더 필터가 켜져 있을 때: 헤더 → LoginUser → @AuthenticationPrincipal 로 들어오는지
// addFilters = false 를 쓰지 않는다 (필터 자체를 확인하는 테스트라서)
@WebMvcTest(controllers = DevHeaderAuthenticationFilterTest.TestController.class,
            properties = "ddip.auth.dev-header.enabled=true")
@Import({DevHeaderAuthenticationFilterTest.TestController.class, SecurityConfig.class})
class DevHeaderAuthenticationFilterTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("헤더가 없으면 기본 고객 1번이 들어온다")
    void noHeader_defaultCustomer() throws Exception {
        mockMvc.perform(get("/test/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.role").value("CUSTOMER"));
    }

    @Test
    @DisplayName("헤더의 id·역할이 그대로 들어온다")
    void headers_owner() throws Exception {
        mockMvc.perform(get("/test/me")
                        .header("X-Test-User-Id", "3")
                        .header("X-Test-Role", "OWNER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(3))
                .andExpect(jsonPath("$.data.role").value("OWNER"));
    }

    @Test
    @DisplayName("역할은 소문자·공백이 섞여도 받는다")
    void role_lowercase() throws Exception {
        mockMvc.perform(get("/test/me")
                        .header("X-Test-User-Id", "5")
                        .header("X-Test-Role", " owner "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("OWNER"));
    }

    @Test
    @DisplayName("id만 있으면 역할은 고객")
    void idOnly_customer() throws Exception {
        mockMvc.perform(get("/test/me").header("X-Test-User-Id", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(7))
                .andExpect(jsonPath("$.data.role").value("CUSTOMER"));
    }

    @Test
    @DisplayName("역할만 있고 id가 없으면 400")
    void roleOnly_badRequest() throws Exception {
        mockMvc.perform(get("/test/me").header("X-Test-Role", "OWNER"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_INVALID_INPUT"));
    }

    @Test
    @DisplayName("id가 숫자가 아니면 400, 공통 응답 모양 그대로")
    void idNotNumber_badRequest() throws Exception {
        mockMvc.perform(get("/test/me").header("X-Test-User-Id", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").value(nullValue()))
                .andExpect(jsonPath("$.error.code").value("COMMON_INVALID_INPUT"))
                .andExpect(jsonPath("$.error.message").value("X-Test-User-Id는 숫자여야 합니다."))
                .andExpect(jsonPath("$.error.fieldErrors").doesNotExist());
    }

    @Test
    @DisplayName("id가 0 이하면 400")
    void idZero_badRequest() throws Exception {
        mockMvc.perform(get("/test/me").header("X-Test-User-Id", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("COMMON_INVALID_INPUT"));
    }

    @Test
    @DisplayName("없는 역할이면 400")
    void unknownRole_badRequest() throws Exception {
        mockMvc.perform(get("/test/me")
                        .header("X-Test-User-Id", "3")
                        .header("X-Test-Role", "BOSS"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("X-Test-Role는 CUSTOMER, OWNER, ADMIN 중 하나여야 합니다."));
    }

    // 테스트 전용 컨트롤러: 받은 LoginUser를 그대로 돌려준다
    @RestController
    static class TestController {

        @GetMapping("/test/me")
        ApiResponse<LoginUser> me(@AuthenticationPrincipal LoginUser user) {
            return ApiResponse.ok(user);
        }
    }
}