package com.main2.delivery.customer.event;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.main2.delivery.customer.client.OwnerOrderNotifier;
import com.main2.delivery.customer.repository.cart.CartRedisRepository;

import lombok.RequiredArgsConstructor;

@Component 
@RequiredArgsConstructor 
public class OrderEventListener {
    private final CartRedisRepository cartRedisRepository;
    private final OwnerOrderNotifier ownerOrderNotifier;

    // 주문 저장이 커밋된 뒤에만 실행 (롤백되면 실행 안됨)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCreated(OrderCreateEvent event){
        cartRedisRepository.clear(event.getUserId());
        ownerOrderNotifier.notfyCreated(event);
    }
}
