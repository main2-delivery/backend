package com.main2.delivery.customer.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 주문이 만들어졌다는 신호 (커밋 후 장바구니 비우기 / 업주 알림에 사용)
@Getter 
@RequiredArgsConstructor 
public class OrderCreateEvent {
    private final Long orderId;
    private final Long storeId;
    private final Long userId;
}
