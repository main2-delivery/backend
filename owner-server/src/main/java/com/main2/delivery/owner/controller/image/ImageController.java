package com.main2.delivery.owner.controller.image;

import com.main2.delivery.common.exception.BusinessException;
import com.main2.delivery.common.exception.CommonErrorCode;
import com.main2.delivery.common.response.ApiResponse;
import com.main2.delivery.common.security.LoginUser;
import com.main2.delivery.owner.dto.image.ImageUploadResponseDto;
import com.main2.delivery.owner.exception.OwnerErrorCode;
import com.main2.delivery.owner.service.image.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

// 이미지 업로드 공통 API (가게 대표 이미지, 메뉴 사진)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/owner/images")
public class ImageController {

    private final ImageService imageService;

    // multipart/form-data, 필드 이름 file
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ImageUploadResponseDto> upload(@AuthenticationPrincipal LoginUser user,
                                                    @RequestPart(value = "file", required = false) MultipartFile file) {
        requireOwner(user);
        return ApiResponse.ok(imageService.upload(file));
    }

    // 업주만 올릴 수 있다 (#32 이후에는 /api/owner/** 주소 규칙으로 막히므로 지워도 된다)
    private void requireOwner(LoginUser user) {
        if (user == null) {
            throw new BusinessException(CommonErrorCode.COMMON_UNAUTHORIZED);
        }
        if (!user.isOwner()) {
            throw new BusinessException(OwnerErrorCode.OWNER_ROLE_REQUIRED);
        }
    }
}
