package com.main2.delivery.owner.dto.menu;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 메뉴 등록·수정 요청
@Getter
@Setter
@NoArgsConstructor
public class MenuRequest {

    @NotBlank(message = "메뉴 이름은 필수입니다.")
    @Size(max = 100, message = "메뉴 이름은 100자 이하입니다.")
    private String name;

    @Size(max = 500, message = "설명은 500자 이하입니다.")
    private String description;

    @NotNull(message = "가격은 필수입니다.")
    @PositiveOrZero(message = "가격은 0 이상이어야 합니다.")
    private Integer price;

    @Size(max = 1000, message = "이미지 주소는 1000자 이하입니다.")
    private String imageUrl;        // 업로드는 이미지 API, 여기서는 URL만 저장
}