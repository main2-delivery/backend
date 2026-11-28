package com.main2.delivery.owner.dto.store;

import com.main2.delivery.core.entity.StoreEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 영업 상태 변경 결과
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StoreStatusResponseDto {
    private Long storeId;
    private String status;

    public static StoreStatusResponseDto from(StoreEntity store) {
        return new StoreStatusResponseDto(store.getId(), store.getStatus());
    }
}
