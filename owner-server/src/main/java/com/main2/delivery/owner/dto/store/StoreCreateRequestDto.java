package com.main2.delivery.owner.dto.store;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 입점 신청 요청 (categoryId 는 카테고리 목록이 정해지기 전까지 선택)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreCreateRequestDto {
    private String name;
    private String address;
    private String phone;
    private Long categoryId;
    private String businessNumber;
}
