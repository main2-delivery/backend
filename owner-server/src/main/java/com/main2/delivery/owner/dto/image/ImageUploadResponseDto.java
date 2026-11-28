package com.main2.delivery.owner.dto.image;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 이미지 업로드 결과: { "imageUrl": "http://localhost:8082/images/2026/11/28/uuid.png" }
// 이 URL 을 가게 정보 수정(logoUrl)·메뉴 등록(imageUrl)에 그대로 넣는다.
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ImageUploadResponseDto {
    private String imageUrl;
}
