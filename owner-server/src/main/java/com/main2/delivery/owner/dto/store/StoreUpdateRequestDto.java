package com.main2.delivery.owner.dto.store;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

// 가게 정보 수정 요청 (PUT: 보낸 값으로 통째로 바꾸고, 이름·주소·영업 상태는 제외)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreUpdateRequestDto {
    private String phone;
    private LocalTime openTime;
    private LocalTime closeTime;
    private Integer minOrderAmount;
    private Integer deliveryFee;
    private Integer deliveryRadiusM;
    private String logoUrl;
}
