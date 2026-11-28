package com.main2.delivery.common.internal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.main2.delivery.common.response.ApiResponse;
import com.main2.delivery.common.security.SecurityConfig;

// /internal/** 은 공유 키가 맞을 때만 통과, 그 밖의 주소는 키와 상관없음
@WebMvcTest(controllers = InternalApiKeyFilterTest.TestController.class,
            properties = "internal.api-key=test-key")
@Import({InternalApiKeyFilterTest.TestController.class, SecurityConfig.class, InternalSecurityConfig.class})
class InternalApiKeyFilterTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("키가 맞으면 통과한다")
    void correctKey_ok() throws Exception {
        mockMvc.perform(get("/internal/test/ping").header(InternalApiKeyFilter.HEADER, "test-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("pong"));
    }

    @Test
    @DisplayName("키가 없으면 401, 공통 응답 모양 그대로")
    void noKey_unauthorized() throws Exception {
        mockMvc.perform(get("/internal/test/ping"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_UNAUTHORIZED"));
    }

    @Test
    @DisplayName("키가 틀리면 401")
    void wrongKey_unauthorized() throws Exception {
        mockMvc.perform(get("/internal/test/ping").header(InternalApiKeyFilter.HEADER, "wrong-key"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("/internal 밖의 주소는 키 없이 통과한다")
    void otherPath_noKeyNeeded() throws Exception {
        mockMvc.perform(get("/test/ping"))
                .andExpect(status().isOk());
    }

    @RestController
    static class TestController {

        @GetMapping("/internal/test/ping")
        ApiResponse<String> internalPing() {
            return ApiResponse.ok("pong");
        }

        @GetMapping("/test/ping")
        ApiResponse<String> publicPing() {
            return ApiResponse.ok("pong");
        }
    }
}