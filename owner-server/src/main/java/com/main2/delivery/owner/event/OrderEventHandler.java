package com.main2.delivery.owner.event;

import org.springframework.stereotype.Component;

import com.main2.delivery.core.event.OrderEvent;

import lombok.extern.slf4j.Slf4j;

// 다른 서버에서 온 주문 이벤트 처리. 처리 내용은 여기에만 둔다
// Sprint 1: InternalOrderEventController가 호출 / Sprint 2(#57): @RabbitListener가 같은 메서드 호출
@Slf4j 
@Component 
public class OrderEventHandler {
    public  void handle(OrderEvent event){
        log.info("주문 이벤트 수신 eventId={} type={} orderId ={} storeId={}",
            event.eventId(), event.eventType(), event.orderId(), event.storeId()
        );

        switch (event.eventType()) {
            case ORDER_CREATED -> handleCreated(event);
            case ORDER_CANCELED -> handleCanceled(event);
            default -> log.warn("업주 서버가 처리하지 않는 이벤트 type={}", event.eventType());
        }
    }

        // 새 주문: 업주 화면 실시간 알림(SSE)은 업주 주문 스토리에서 여기에 붙임
        private void handleCreated(OrderEvent event){
        }

        // 접수 전 취소: 업주 화면에서 주문 내리기는 업주 주문 스토리에서 여기에 붙임
        private void handleCanceled(OrderEvent event){
        }
}
