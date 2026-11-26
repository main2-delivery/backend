package com.main2.delivery.owner.dto.store;

import com.main2.delivery.core.entity.StoreEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 내 가게 목록의 한 줄 (업주 웹 상단 가게 선택용)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreSummaryResponse {
    private Long storeId;
    private String name;
    private String address;
    private String status;

    public static StoreSummaryResponse from(StoreEntity store) {
        return StoreSummaryResponse.builder()
                .storeId(store.getId())
                .name(store.getStoreName())
                .address(store.getAddress())
                .status(store.getStatus())
                .build();
    }
}
