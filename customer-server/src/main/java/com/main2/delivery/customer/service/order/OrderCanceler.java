package com.main2.delivery.customer.service.order;

import java.time.LocalDateTime;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.main2.delivery.common.exception.BusinessException;
import com.main2.delivery.core.entity.OrderEntity;
import com.main2.delivery.core.entity.OrderStatus;
import com.main2.delivery.core.event.OrderEvent; 
import com.main2.delivery.customer.exception.OrderErrorCode;
import com.main2.delivery.customer.repository.order.OrderRepository;

import lombok.RequiredArgsConstructor;

// 주문 취소 (트랜젝션 단위). 낙관적 락 예외는 커밋 때 터지므로 바깥(OrderService)에서 잡음
@Component 
@RequiredArgsConstructor 
public class OrderCanceler {
    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional 
    public OrderEntity cancel(Long userId, Long orderId, String reason){
        // [1] 내 주문인지 (남의 주문이면 404)
        OrderEntity order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(()->new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

        // [2] 접수 전(PENDING)만 취소 가능
        if(order.getStatus() != OrderStatus.PENDING){
            throw new BusinessException(OrderErrorCode.ORDER_NOT_CANCELABLE);
        }

        // [3] 상태 변경 (커밋 때 UPDATE ... WHERE version = ? 로 나감)
        order.setStatus(OrderStatus.CANCELED);
        order.setCancelReason(reason); 
        order.setCanceledAt(LocalDateTime.now());

        // [4] 커밋되면 업주 알림 + 추적 화면 전달 (reason은 payload에 담김)
        eventPublisher.publishEvent(OrderEvent.canceled(order));
        return order;
    }
}
