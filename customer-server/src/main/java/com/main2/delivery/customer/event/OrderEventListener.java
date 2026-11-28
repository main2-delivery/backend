package com.main2.delivery.customer.event;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.main2.delivery.core.event.OrderEvent;
import com.main2.delivery.core.event.OrderEventNotifier;
import com.main2.delivery.customer.dto.tracking.OrderStatusMessage;
import com.main2.delivery.customer.repository.cart.CartRedisRepository;
import com.main2.delivery.customer.service.tracking.TrackingService;

import lombok.RequiredArgsConstructor;

// 이 서버에서 발행한 주문 이벤트를 커밋 후에 처리 (롤백되면 실행 안 됨)
@Component
@RequiredArgsConstructor
public class OrderEventListener {
    private final CartRedisRepository cartRedisRepository;
    private final TrackingService trackingService;
    private final OrderEventNotifier orderEventNotifier;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderEvent(OrderEvent event) {
        switch (event.eventType()) {
            // 주문 생성: 장바구니 비우기
            case ORDER_CREATED -> cartRedisRepository.clear(event.userId());
            // 고객 취소: 추적 화면에 CANCELED 전달
            case ORDER_CANCELED -> trackingService.push(new OrderStatusMessage(
                    event.orderId(), event.status().name(), null, null, (String) event.payload().get("reason")));
            default -> { }
        }
        orderEventNotifier.send(event);   // 업주 서버로
    }
}