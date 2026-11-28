package com.main2.delivery.owner.controller.store;

import com.main2.delivery.owner.dto.store.StoreCreateResponseDto;
import com.main2.delivery.owner.exception.OwnerErrorCode;
import com.main2.delivery.common.exception.BusinessException;
import com.main2.delivery.common.security.SecurityConfig;
import com.main2.delivery.owner.service.store.StoreService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// HTTP 요청 단계 확인: 상태 코드, 로그인 사용자, 에러 응답 형식
// 테스트 헤더 → LoginUser 변환까지 확인하려고 개발용 헤더 필터를 켠다 (docs/auth.md 5장)
@WebMvcTest(controllers = StoreController.class, properties = "ddip.auth.dev-header.enabled=true")
@Import(SecurityConfig.class)
class StoreControllerTest {

    private static final String CREATE_BODY = """
            {"name":"띱치킨 안양점","address":"경기도 안양시 동안구 시민대로 100","phone":"031-123-4567",
             "businessNumber":"123-45-67890"}""";

    @Autowired MockMvc mockMvc;
    @MockitoBean StoreService storeService;

    private MockHttpServletRequestBuilder asOwner(MockHttpServletRequestBuilder builder) {
        return builder.header("X-Test-User-Id", "1").header("X-Test-Role", "OWNER")
                .contentType(MediaType.APPLICATION_JSON);
    }

    @Test
    @DisplayName("입점 신청 성공 시 201과 가게 번호·좌표를 돌려준다")
    void apply_returns201() throws Exception {
        given(storeService.apply(eq(1L), any())).willReturn(
                new StoreCreateResponseDto(12L, new BigDecimal("37.3943000"), new BigDecimal("126.9568000")));

        mockMvc.perform(asOwner(post("/api/owner/stores")).content(CREATE_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.storeId").value(12))
                .andExpect(jsonPath("$.data.lat").value(37.3943));
    }

    @Test
    @DisplayName("서비스에서 형식 오류를 던지면 400과 에러 코드")
    void apply_invalidInput() throws Exception {
        given(storeService.apply(eq(1L), any())).willThrow(new BusinessException(OwnerErrorCode.STORE_INVALID_BUSINESS_NUMBER));

        mockMvc.perform(asOwner(post("/api/owner/stores")).content(CREATE_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("STORE_INVALID_BUSINESS_NUMBER"));
    }

    @Test
    @DisplayName("영업시간 형식이 HH:mm 이 아니면 400")
    void update_invalidTimeFormat() throws Exception {
        String body = """
                {"phone":"031-123-4567","openTime":"11시","closeTime":"23:00",
                 "minOrderAmount":15000,"deliveryFee":3000,"deliveryRadiusM":3000}""";

        mockMvc.perform(asOwner(put("/api/owner/stores/10")).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("COMMON_INVALID_JSON"));
    }

    @Test
    @DisplayName("다른 업주의 가게를 조회하면 403")
    void get_otherOwnersStore() throws Exception {
        given(storeService.getMyStore(1L, 10L)).willThrow(new BusinessException(OwnerErrorCode.STORE_NOT_OWNER));

        mockMvc.perform(asOwner(get("/api/owner/stores/10")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("STORE_NOT_OWNER"));
    }

    @Test
    @DisplayName("업주가 아니면 403 (헤더가 없으면 기본 고객 1번으로 들어온다)")
    void requiresOwner() throws Exception {
        mockMvc.perform(get("/api/owner/stores"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("OWNER_ROLE_REQUIRED"));
        mockMvc.perform(get("/api/owner/stores").header("X-Test-User-Id", "1").header("X-Test-Role", "CUSTOMER"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("OWNER_ROLE_REQUIRED"));
    }

    @Test
    @DisplayName("테스트 헤더 값이 잘못되면 필터에서 400")
    void invalidTestHeader() throws Exception {
        mockMvc.perform(get("/api/owner/stores").header("X-Test-User-Id", "abc").header("X-Test-Role", "OWNER"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("COMMON_INVALID_INPUT"));
    }
}
