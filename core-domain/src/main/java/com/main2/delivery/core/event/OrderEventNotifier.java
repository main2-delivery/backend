package com.main2.delivery.core.event;

// 주문 이벤트를 다른 서버로 보내는 창구. 서비스·리스너는 이 인터페이스만 의존
// Sprint 1: REST 구현체 / Sprint 2(#57): RabbitMQ 구현체로 교체
public interface OrderEventNotifier {

    // 실패해도 예외를 던지지 않음 (보내는 쪽 작업은 이미 커밋됨)
    void send(OrderEvent event);
}