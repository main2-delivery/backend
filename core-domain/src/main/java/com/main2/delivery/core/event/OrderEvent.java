package com.main2.delivery.core.event;

import com.main2.delivery.core.entity.OrderEntity;
import com.main2.delivery.core.entity.OrderStatus;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

// 서버 간 주문 이벤트. Sprint 1은 REST 본문, Sprint 2는 RabbitMQ 메시지로 그대로 씀 (#8 메시지 형식)
public record OrderEvent(
        String eventId,          // 재시도로 두 번 와도 한 번만 처리하기 위한 번호
        OrderEventType eventType,
        Instant occurredAt,      // 서버끼리 주고받는 시각은 시간대 없는 Instant로
        Long orderId,
        Long storeId,
        Long userId,
        OrderStatus status,
        Map<String, Object> payload
) {
    // payload가 없으면 빈 맵, 있으면 바꿀 수 없는 복사본으로
    public OrderEvent {
        payload = (payload == null) ? Map.of() : Map.copyOf(payload);
    }

    // 주문 엔티티로 만들기 (트랜잭션 안에서 부를 것. store·user는 id만 읽어서 추가 조회 없음)
    public static OrderEvent of(OrderEventType type, OrderEntity order, Map<String, Object> payload) {
        return new OrderEvent(
                UUID.randomUUID().toString(),
                type,
                Instant.now(),
                order.getId(),
                order.getStore().getId(),
                order.getUser().getId(),
                order.getStatus(),
                payload);
    }

    public static OrderEvent created(OrderEntity order) {
        return of(OrderEventType.ORDER_CREATED, order, Map.of());
    }

    public static OrderEvent canceled(OrderEntity order) {
        Map<String, Object> payload = (order.getCancelReason() == null)
                ? Map.of()
                : Map.of("reason", order.getCancelReason());
        return of(OrderEventType.ORDER_CANCELED, order, payload);
    }
}