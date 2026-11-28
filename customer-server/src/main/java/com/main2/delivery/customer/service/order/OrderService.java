package com.main2.delivery.customer.service.order;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.main2.delivery.common.exception.BusinessException;
import com.main2.delivery.common.exception.CommonErrorCode;
import com.main2.delivery.core.entity.OrderEntity;
import com.main2.delivery.core.entity.OrderItemEntity;
import com.main2.delivery.customer.dto.order.OrderCancelRequest;
import com.main2.delivery.customer.dto.order.OrderCancelResponse;
import com.main2.delivery.customer.dto.order.OrderCreateRequest;
import com.main2.delivery.customer.dto.order.OrderCreateResponse;
import com.main2.delivery.customer.dto.order.OrderDetailResponse;
import com.main2.delivery.customer.dto.order.OrderListResponse;
import com.main2.delivery.customer.dto.order.OrderSummaryResponse;
import com.main2.delivery.customer.exception.OrderErrorCode;
import com.main2.delivery.customer.repository.order.OrderItemRepository;
import com.main2.delivery.customer.repository.order.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderCreator orderCreator;
    private final OrderItemRepository orderItemRepository;
    private final OrderCanceler orderCanceler;

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

    // [2] 내 주문 목록 (최신순)
    @Transactional (readOnly = true)
    public OrderListResponse getOrders(Long userId, int page, int size){
        if(page < 0 || size < 1 || size > 50){
            throw new BusinessException(CommonErrorCode.COMMON_INVALID_INPUT, "page는 0이상 , size는 1~50");
        }
        // 주문 + 가게 (EntityGraph로 한번에)
        Page<OrderEntity> result = orderRepository.findByUserIdOrderByIdDesc(userId, PageRequest.of(page, size));

        // 이 페이지 주문들의 메뉴를 IN 쿼리 한번으로 가져와서 주문별로 묶음
        List<Long> orderIds = result.getContent().stream().map(OrderEntity::getId).toList();
        Map<Long, List<OrderItemEntity>> itemsByOrder = orderIds.isEmpty()
                ? Map.of()
                : orderItemRepository.findByOrderIdIn(orderIds).stream()
                        .collect(Collectors.groupingBy(item -> item.getOrder().getId()));

        List<OrderSummaryResponse> orders = result.getContent().stream()
                .map(order -> new OrderSummaryResponse(order, itemsByOrder.getOrDefault(order.getId(), List.of())))
                .toList();
        return new OrderListResponse(result, orders);
    }

    // [3] 주문 상세 (남의 주문이면 404)
    @Transactional (readOnly = true)
    public OrderDetailResponse getOrder(Long userId, Long orderId){
        OrderEntity order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(()->new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
        List<OrderItemEntity> items = orderItemRepository.findByOrderId(orderId);
        return new OrderDetailResponse(order, items);
    }

    // [4] 접수 전 주문 취소
    public OrderCancelResponse cancelOrder(Long userId, Long orderId, OrderCancelRequest req){
        String reason = (req == null || req.getReason() == null || req.getReason().isBlank())
                ? "고객 요청"
                : req.getReason().trim();
        if(reason.length() > 255){
            throw new BusinessException(CommonErrorCode.COMMON_INVALID_INPUT, "취소 사유는 255자 이하");
        }
        try{
            return new OrderCancelResponse(orderCanceler.cancel(userId, orderId, reason));
        }catch(ObjectOptimisticLockingFailureException e){
            // 점주 접수와 동시에 들어와서 version이 먼저 바뀐 경우 -> 409
            throw new BusinessException(OrderErrorCode.ORDER_CONCURRENT_UPDATE);
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
