package com.main2.delivery.customer.dto.order;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@NoArgsConstructor 
public class OrderCancelRequest {
    private String reason;  // 취소 사유 (없으면 "고객 요청")
}
