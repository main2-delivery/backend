package com.main2.delivery.owner.service.image;

import com.main2.delivery.common.exception.BusinessException;
import com.main2.delivery.owner.dto.image.ImageUploadResponseDto;
import com.main2.delivery.owner.exception.OwnerErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

// 이미지 업로드 공통 처리 (가게 대표 이미지, 메뉴 사진 모두 사용)
@Slf4j
@Service
public class ImageService {

    public static final long MAX_BYTES = 5L * 1024 * 1024;   // 5MB. application.yml 의 multipart 제한과 같은 값

    private final ImageStorage imageStorage;
    private final String publicBaseUrl;

    public ImageService(ImageStorage imageStorage,
                        @Value("${app.image.base-url}") String baseUrl) {
        this.imageStorage = imageStorage;
        // 앱·웹 어디서든 열리도록 절대 주소로 돌려준다 (끝의 / 는 정리)
        this.publicBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    public ImageUploadResponseDto upload(MultipartFile file) {
        // [1] 파일이 있는지
        if (file == null || file.isEmpty()) {
            throw new BusinessException(OwnerErrorCode.IMAGE_EMPTY);
        }
        // [2] 크기 (multipart 설정에서 먼저 막히지만, 설정이 바뀌어도 지켜지도록 한 번 더)
        if (file.getSize() > MAX_BYTES) {
            throw new BusinessException(OwnerErrorCode.IMAGE_TOO_LARGE);
        }
        // [3] 확장자
        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (extension == null) {
            throw new BusinessException(OwnerErrorCode.IMAGE_INVALID_TYPE);
        }
        // [4] 실제 내용이 이미지인지 (시그니처) + 확장자와 일치하는지
        byte[] bytes = readBytes(file);
        ImageType type = ImageType.detect(bytes)
                .filter(t -> t.allows(extension))
                .orElseThrow(() -> new BusinessException(OwnerErrorCode.IMAGE_INVALID_TYPE));

        // [5] 저장 → 공개 URL
        String path = imageStorage.save(bytes, type.savedExtension());
        return new ImageUploadResponseDto(publicBaseUrl + "/images/" + path);
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            log.warn("업로드 파일 읽기 실패: {}", e.getMessage());
            throw new BusinessException(OwnerErrorCode.IMAGE_EMPTY);
        }
    }
}
