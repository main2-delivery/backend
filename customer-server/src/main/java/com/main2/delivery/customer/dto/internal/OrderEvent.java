package com.main2.delivery.customer.dto.internal;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 서버 간 주문 이벤트 (DUDU-8 형식. Sprint 2에 RabbitMQ 메시지로 그대로 사용)
// 공통 모듈(DUDU-12)에 OrderEvent가 생기면 그걸로 교체
@Getter
@Setter
@NoArgsConstructor
public class OrderEvent {
    private String eventId;             // 중복 처리 방지 키 (uuid)
    private String eventType;           // ORDER_STATUS_CHANGED 등
    private LocalDateTime occurredAt;   // 업주 서버에서 상태가 바뀐 시각
    private Long orderId;
    private Long storeId;
    private Long userId;
    private String status;              // 바뀐 상태 (OrderStatus 이름)
    private Map<String, Object> payload; // 접수: cookingMinutes / 거절: reason
}