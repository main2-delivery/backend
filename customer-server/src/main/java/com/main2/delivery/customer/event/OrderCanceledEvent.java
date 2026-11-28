package com.main2.delivery.customer.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 주문이 취소됐다는 신호 (커밋 후 업주 알림에 사용)
@Getter 
@RequiredArgsConstructor 
public class OrderCanceledEvent {
    private final Long orderId;
    private final Long storeId;
    private final String reason;    // 취소 사유
}
