package com.main2.delivery.owner.controller.image;

import com.main2.delivery.common.security.SecurityConfig;
import com.main2.delivery.owner.service.image.ImageService;
import com.main2.delivery.owner.service.image.LocalImageStorage;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 완료 기준 "업로드 후 받은 URL 로 이미지가 열림" 확인: 실제 서비스·저장소로 올리고, 받은 주소로 다시 GET
@WebMvcTest(controllers = ImageController.class, properties = "ddip.auth.dev-header.enabled=true")
@Import({SecurityConfig.class, ImageService.class, LocalImageStorage.class})
class ImageUploadFlowTest {

    private static final byte[] PNG = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 13};
    private static final String BASE_URL = "http://localhost:8082";

    @TempDir static Path uploadDir;

    @DynamicPropertySource
    static void imageProperties(DynamicPropertyRegistry registry) {
        registry.add("app.image.upload-dir", uploadDir::toString);
        registry.add("app.image.base-url", () -> BASE_URL);
    }

    @Autowired MockMvc mockMvc;

    @Test
    @DisplayName("업로드 응답의 imageUrl 로 GET 하면 올린 이미지 바이트가 그대로 내려온다")
    void uploadedImageIsServed() throws Exception {
        String body = mockMvc.perform(multipart("/api/owner/images")
                        .file(new MockMultipartFile("file", "logo.png", "image/png", PNG))
                        .header("X-Test-User-Id", "1").header("X-Test-Role", "OWNER"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String imageUrl = JsonPath.read(body, "$.data.imageUrl");

        mockMvc.perform(get(imageUrl.substring(BASE_URL.length())))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(content().bytes(PNG));
    }

    @Test
    @DisplayName("없는 이미지 주소는 404")
    void missingImage404() throws Exception {
        mockMvc.perform(get("/images/2026/01/01/nothing.png"))
                .andExpect(status().isNotFound());
    }
}
