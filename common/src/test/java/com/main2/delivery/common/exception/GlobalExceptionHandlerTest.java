package com.main2.delivery.common.exception;

import com.main2.delivery.common.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GlobalExceptionHandlerTest.TestController.class)
@AutoConfigureMockMvc(addFilters = false) 
@Import(GlobalExceptionHandlerTest.TestController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void 성공_응답은_success_true와_data를_담는다() throws Exception {
        mockMvc.perform(get("/test/ok"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value("pong"))
                .andExpect(jsonPath("$.error").value(nullValue()));
    }

    @Test
    void 비즈니스_예외는_ErrorCode의_상태와_코드로_응답한다() throws Exception {
        mockMvc.perform(get("/test/business"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").value(nullValue()))
                .andExpect(jsonPath("$.error.code").value("COMMON_CONFLICT"))
                .andExpect(jsonPath("$.error.message").value("요청이 현재 상태와 충돌합니다."))
                .andExpect(jsonPath("$.error.fieldErrors").doesNotExist());
    }

    @Test
    void 바디_검증_실패는_400과_fieldErrors를_담는다() throws Exception {
        mockMvc.perform(post("/test/body")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\",\"quantity\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("COMMON_INVALID_INPUT"))
                .andExpect(jsonPath("$.error.fieldErrors", hasSize(2)));
    }

    @Test
    void JSON_문법_오류는_INVALID_JSON으로_응답한다() throws Exception {
        mockMvc.perform(post("/test/body")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{name"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("COMMON_INVALID_JSON"));
    }

    @Test
    void 파라미터_검증_실패는_400과_fieldErrors를_담는다() throws Exception {
        mockMvc.perform(get("/test/param").param("page", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("COMMON_INVALID_INPUT"))
                .andExpect(jsonPath("$.error.fieldErrors[0].field").value("page"));
    }

    @Test
    void 파라미터_타입_오류는_400으로_응답한다() throws Exception {
        mockMvc.perform(get("/test/param").param("page", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("COMMON_INVALID_INPUT"));
    }

    @Test
    void 필수_파라미터_누락은_400으로_응답한다() throws Exception {
        mockMvc.perform(get("/test/param"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("COMMON_INVALID_INPUT"));
    }

    @Test
    void 없는_주소는_404로_응답한다() throws Exception {
        mockMvc.perform(get("/test/nowhere"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("COMMON_NOT_FOUND"));
    }

    @Test
    void 지원하지_않는_요청_방식은_405로_응답한다() throws Exception {
        mockMvc.perform(delete("/test/ok"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.error.code").value("COMMON_METHOD_NOT_ALLOWED"));
    }

    @Test
    void 예상하지_못한_예외는_500으로_응답하고_원인을_숨긴다() throws Exception {
        mockMvc.perform(get("/test/unknown"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error.code").value("COMMON_INTERNAL_ERROR"))
                .andExpect(jsonPath("$.error.message").value("서버 오류가 발생했습니다."));
    }

    // ===== 테스트에서만 쓰는 컨트롤러 =====

    @RestController
    @RequestMapping("/test")
    static class TestController {

        @GetMapping("/ok")
        public ApiResponse<Map<String, String>> ok() {
            return ApiResponse.ok(Map.of("message", "pong"));
        }

        @GetMapping("/business")
        public ApiResponse<Void> business() {
            throw new BusinessException(CommonErrorCode.COMMON_CONFLICT);
        }

        @PostMapping("/body")
        public ApiResponse<SampleRequest> body(@Valid @RequestBody SampleRequest request) {
            return ApiResponse.ok(request);
        }

        @GetMapping("/param")
        public ApiResponse<Integer> param(@RequestParam @Min(1) int page) {
            return ApiResponse.ok(page);
        }

        @GetMapping("/unknown")
        public ApiResponse<Void> unknown() {
            throw new IllegalStateException("내부 버그 상세 내용");
        }
    }

    record SampleRequest(@NotBlank String name, @Min(1) int quantity) {
    }
}