package com.main2.delivery.owner.dto.store;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 영업 상태 변경 요청: { "status": "OPEN" }
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreStatusRequestDto {
    private String status;
}
