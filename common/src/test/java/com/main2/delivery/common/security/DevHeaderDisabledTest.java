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

// 설정을 안 켜면(기본값 false) 헤더를 보내도 사용자가 들어오지 않는다
@WebMvcTest(controllers = DevHeaderDisabledTest.TestController.class)
@Import({DevHeaderDisabledTest.TestController.class, SecurityConfig.class})
class DevHeaderDisabledTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("설정이 꺼져 있으면 헤더를 보내도 LoginUser는 null")
    void disabled_ignoresHeaders() throws Exception {
        mockMvc.perform(get("/test/me")
                        .header("X-Test-User-Id", "3")
                        .header("X-Test-Role", "OWNER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @RestController
    static class TestController {

        @GetMapping("/test/me")
        ApiResponse<LoginUser> me(@AuthenticationPrincipal LoginUser user) {
            return ApiResponse.ok(user);
        }
    }
}