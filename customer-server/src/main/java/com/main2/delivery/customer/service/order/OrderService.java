package com.main2.delivery.customer.service.order;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.main2.delivery.common.exception.BusinessException;
import com.main2.delivery.common.exception.CommonErrorCode;
import com.main2.delivery.core.entity.OrderEntity;
import com.main2.delivery.customer.dto.order.OrderCreateRequest;
import com.main2.delivery.customer.dto.order.OrderCreateResponse;
import com.main2.delivery.customer.exception.OrderErrorCode;
import com.main2.delivery.customer.repository.order.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderCreator orderCreator;

    // [1] 주문 생성 (Sprint1: 결제 없이 바로 접수 대기)
    public OrderCreateResponse createOrder(Long userId, OrderCreateRequest req){
        checkRequest(req);

        // 같은 키로 이미 만든 주문이 있으면 그대로 반환(버튼 연타-재시도)
        OrderEntity existing = findByOrderKey(userId, req.getOrderKey());
        if(existing != null){
            return new OrderCreateResponse(existing);
        }
        
        try{
            return new OrderCreateResponse(orderCreator.create(userId, req));
        }catch(DataIntegrityViolationException e){
            // 같은 키로 '동시에' 들어온 두 번째 요청: DB UNIQUE에 막힘 -> 먼저 저장된 주문 반환
            OrderEntity saved = findByOrderKey(userId, req.getOrderKey());
            if(saved == null){
                throw e;
            }
            return new OrderCreateResponse(saved);
        }
    }

    // 같은 주문 키의 주문(다른 사람이 쓴 거면 409)
    private OrderEntity findByOrderKey(Long userId, String orderKey){
        OrderEntity order = orderRepository.findByOrderKey(orderKey).orElse(null);
        if(order != null && !order.getUser().getId().equals(userId)){
            throw new BusinessException(OrderErrorCode.ORDER_KEY_CONFLICT);
        }
        return order;
    }

    // 필수값 확인 (없으면 409)
    private void checkRequest(OrderCreateRequest req){
        if(req.getOrderKey() == null || req.getOrderKey().isBlank() || req.getOrderKey().length() > 64 || req.getAddressId() == null){
            throw new BusinessException(CommonErrorCode.COMMON_INVALID_INPUT, "주문 키와 배달 주소는 필수");
        }
        if(!"CARD".equals(req.getPaymentMethod()) && !"EASY_PAY".equals(req.getPaymentMethod())){
            throw new BusinessException(CommonErrorCode.COMMON_INVALID_INPUT, "결제 수단은 CARD 또는 EASY_PAY");
        }
    }
}
