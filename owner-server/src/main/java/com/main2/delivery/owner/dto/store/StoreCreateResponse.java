package com.main2.delivery.owner.dto.store;

import com.main2.delivery.core.entity.StoreEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// 입점 신청 결과: 새 가게 번호와 변환된 좌표
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreCreateResponse {
    private Long storeId;
    private BigDecimal lat;
    private BigDecimal lng;

    public static StoreCreateResponse from(StoreEntity store) {
        return StoreCreateResponse.builder()
                .storeId(store.getId())
                .lat(store.getLat())
                .lng(store.getLng())
                .build();
    }
}
