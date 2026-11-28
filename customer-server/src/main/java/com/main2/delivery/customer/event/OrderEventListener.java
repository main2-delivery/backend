package com.main2.delivery.customer.event;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.main2.delivery.customer.client.OwnerOrderNotifier;
import com.main2.delivery.customer.repository.cart.CartRedisRepository;
import com.main2.delivery.customer.dto.tracking.OrderStatusMessage;
import com.main2.delivery.customer.service.tracking.TrackingService;

import lombok.RequiredArgsConstructor;

@Component 
@RequiredArgsConstructor 
public class OrderEventListener {
    private final CartRedisRepository cartRedisRepository;
    private final OwnerOrderNotifier ownerOrderNotifier;
    private final TrackingService trackingService;

    // 주문 저장이 커밋된 뒤에만 실행 (롤백되면 실행 안됨)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCreated(OrderCreateEvent event){
        cartRedisRepository.clear(event.getUserId());
        ownerOrderNotifier.notfyCreated(event);
    }

    // 주문 취소가 커밋된 뒤에만 실행: 업주 알림 + 추적 화면에 전달
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCanceled(OrderCanceledEvent event){
        ownerOrderNotifier.notifyCanceled(event);
        trackingService.push(new OrderStatusMessage(
                event.getOrderId(), "CANCELED", null, null, event.getReason()));
    }
}
