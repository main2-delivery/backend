package com.main2.delivery.owner.service.image;

import com.main2.delivery.common.exception.BusinessException;
import com.main2.delivery.common.exception.ErrorCode;
import com.main2.delivery.owner.dto.image.ImageUploadResponseDto;
import com.main2.delivery.owner.exception.OwnerErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// 이미지 업로드 규칙 확인 (임시 폴더에 실제로 저장해 본다)
class ImageServiceTest {

    private static final byte[] PNG = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};
    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0x10};
    private static final byte[] WEBP = "RIFF\0\0\0\0WEBPVP8 ".getBytes(StandardCharsets.US_ASCII);

    @TempDir Path uploadDir;
    ImageService imageService;

    @BeforeEach
    void setUp() {
        imageService = new ImageService(new LocalImageStorage(uploadDir.toString()), "http://localhost:8082/");
    }

    private MockMultipartFile file(String name, byte[] content) {
        return new MockMultipartFile("file", name, "image/png", content);
    }

    private void assertError(Runnable action, ErrorCode expected) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(BusinessException.class, e -> assertThat(e.getErrorCode()).isEqualTo(expected));
    }

    @Test
    @DisplayName("PNG 를 올리면 저장되고, 받은 URL 의 경로에 실제 파일이 있다")
    void upload_png() throws Exception {
        ImageUploadResponseDto response = imageService.upload(file("logo.PNG", PNG));

        assertThat(response.getImageUrl()).startsWith("http://localhost:8082/images/").endsWith(".png");
        String relative = response.getImageUrl().substring("http://localhost:8082/images/".length());
        Path saved = uploadDir.resolve(relative);
        assertThat(Files.readAllBytes(saved)).isEqualTo(PNG);
    }

    @Test
    @DisplayName("jpeg·webp 도 올라가고, 저장 확장자는 정해진 값으로 통일된다")
    void upload_jpegAndWebp() {
        assertThat(imageService.upload(file("menu.jpeg", JPEG)).getImageUrl()).endsWith(".jpg");
        assertThat(imageService.upload(file("menu.webp", WEBP)).getImageUrl()).endsWith(".webp");
    }

    @Test
    @DisplayName("확장자만 이미지인 텍스트 파일은 400")
    void upload_fakeImage() {
        byte[] text = "hello, not an image".getBytes(StandardCharsets.UTF_8);

        assertError(() -> imageService.upload(file("evil.png", text)), OwnerErrorCode.IMAGE_INVALID_TYPE);
    }

    @Test
    @DisplayName("내용은 PNG 인데 확장자가 다르거나 없으면 400")
    void upload_extensionMismatch() {
        assertError(() -> imageService.upload(file("logo.jpg", PNG)), OwnerErrorCode.IMAGE_INVALID_TYPE);
        assertError(() -> imageService.upload(file("logo.exe", PNG)), OwnerErrorCode.IMAGE_INVALID_TYPE);
        assertError(() -> imageService.upload(file("logo", PNG)), OwnerErrorCode.IMAGE_INVALID_TYPE);
    }

    @Test
    @DisplayName("파일이 없거나 비어 있으면 400")
    void upload_empty() {
        assertError(() -> imageService.upload(null), OwnerErrorCode.IMAGE_EMPTY);
        assertError(() -> imageService.upload(file("logo.png", new byte[0])), OwnerErrorCode.IMAGE_EMPTY);
    }

    @Test
    @DisplayName("5MB 를 넘으면 400")
    void upload_tooLarge() {
        byte[] big = new byte[(int) ImageService.MAX_BYTES + 1];
        System.arraycopy(PNG, 0, big, 0, PNG.length);

        assertError(() -> imageService.upload(file("big.png", big)), OwnerErrorCode.IMAGE_TOO_LARGE);
    }

    @Test
    @DisplayName("업로드 파일 이름에 경로가 섞여 있어도 업로드 폴더 안에만 저장된다")
    void upload_pathTraversalName() throws Exception {
        ImageUploadResponseDto response = imageService.upload(file("../../etc/logo.png", PNG));

        String relative = response.getImageUrl().substring("http://localhost:8082/images/".length());
        assertThat(relative).doesNotContain("..");
        assertThat(uploadDir.resolve(relative).normalize()).startsWith(uploadDir);
        assertThat(Files.exists(uploadDir.resolve(relative))).isTrue();
    }
}
