package com.main2.delivery.owner.controller.image;

import com.main2.delivery.common.exception.BusinessException;
import com.main2.delivery.common.security.SecurityConfig;
import com.main2.delivery.owner.dto.image.ImageUploadResponseDto;
import com.main2.delivery.owner.exception.OwnerErrorCode;
import com.main2.delivery.owner.service.image.ImageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 이미지 업로드 HTTP 단계 확인: 201, 로그인 헤더, 업로드 예외가 500 이 아닌 400 으로 나가는지
@WebMvcTest(controllers = ImageController.class, properties = "ddip.auth.dev-header.enabled=true")
@Import(SecurityConfig.class)
class ImageControllerTest {

    private static final MockMultipartFile FILE =
            new MockMultipartFile("file", "logo.png", "image/png", new byte[]{1, 2, 3});

    @Autowired MockMvc mockMvc;
    @MockitoBean ImageService imageService;

    @Test
    @DisplayName("업로드 성공 시 201과 imageUrl")
    void upload_returns201() throws Exception {
        given(imageService.upload(any())).willReturn(new ImageUploadResponseDto("http://localhost:8082/images/a.png"));

        mockMvc.perform(multipart("/api/owner/images").file(FILE)
                        .header("X-Test-User-Id", "1").header("X-Test-Role", "OWNER"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.imageUrl").value("http://localhost:8082/images/a.png"));
    }

    @Test
    @DisplayName("이미지가 아니면 400과 에러 코드")
    void upload_invalidType() throws Exception {
        given(imageService.upload(any())).willThrow(new BusinessException(OwnerErrorCode.IMAGE_INVALID_TYPE));

        mockMvc.perform(multipart("/api/owner/images").file(FILE)
                        .header("X-Test-User-Id", "1").header("X-Test-Role", "OWNER"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("IMAGE_INVALID_TYPE"));
    }

    @Test
    @DisplayName("크기 제한 초과 예외는 500 이 아니라 400 IMAGE_TOO_LARGE")
    void upload_tooLarge() throws Exception {
        given(imageService.upload(any())).willThrow(new MaxUploadSizeExceededException(5L * 1024 * 1024));

        mockMvc.perform(multipart("/api/owner/images").file(FILE)
                        .header("X-Test-User-Id", "1").header("X-Test-Role", "OWNER"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("IMAGE_TOO_LARGE"));
    }

    @Test
    @DisplayName("업주가 아니면 403 (헤더가 없으면 기본 고객 1번으로 들어온다)")
    void upload_requiresOwner() throws Exception {
        mockMvc.perform(multipart("/api/owner/images").file(FILE))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("OWNER_ROLE_REQUIRED"));
        mockMvc.perform(multipart("/api/owner/images").file(FILE)
                        .header("X-Test-User-Id", "1").header("X-Test-Role", "CUSTOMER"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("OWNER_ROLE_REQUIRED"));
    }
}
