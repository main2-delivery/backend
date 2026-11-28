package com.main2.delivery.customer.service.order;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;   
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.main2.delivery.common.exception.BusinessException;
import com.main2.delivery.common.exception.CommonErrorCode;
import com.main2.delivery.core.entity.MenuEntity;
import com.main2.delivery.core.entity.OrderEntity;
import com.main2.delivery.core.entity.OrderItemEntity;
import com.main2.delivery.core.entity.StoreEntity;
import com.main2.delivery.core.entity.UserAddressEntity;
import com.main2.delivery.core.entity.UserEntity;
import com.main2.delivery.customer.dto.order.OrderCreateRequest;
import com.main2.delivery.core.event.OrderEvent;
import com.main2.delivery.customer.exception.OrderErrorCode;
import com.main2.delivery.customer.repository.address.UserAddressRepository;
import com.main2.delivery.customer.repository.cart.CartRedisRepository;
import com.main2.delivery.customer.repository.order.OrderItemRepository;
import com.main2.delivery.customer.repository.order.OrderRepository;
import com.main2.delivery.customer.repository.store.MenuRepository;
import com.main2.delivery.customer.repository.store.StoreRepository;
import com.main2.delivery.customer.repository.user.UserRepository;
import com.main2.delivery.customer.util.DistanceUtils;

import lombok.RequiredArgsConstructor;

// 주문 검증-금액 계산-저장 (하나의 트랜젝션)
@Component 
@RequiredArgsConstructor 
public class OrderCreator {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;
    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;
    private final CartRedisRepository cartRedisRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional 
    public OrderEntity create(Long userId, OrderCreateRequest req){
        UserEntity user = userRepository.findById(userId).orElseThrow(()->new BusinessException(CommonErrorCode.COMMON_NOT_FOUND, "사용자 없음"));

        // 내 주소만 (남의 주소 번호면 404)
        UserAddressEntity address = userAddressRepository.findByIdAndUserId(req.getAddressId(), userId)
                .orElseThrow(()->new BusinessException(OrderErrorCode.ORDER_ADDRESS_NOT_FOUND));

        // 장바구니 읽기
        Long storeId = cartRedisRepository.findStoreId(userId);
        Map<String, Integer> cartItems = cartRedisRepository.findItems(userId);
        if(storeId == null || cartItems.isEmpty()){
            throw new BusinessException(OrderErrorCode.ORDER_CART_EMPTY);
        }

        // 가게 검증: 영업 중 + 배달 반경 안
        StoreEntity store = storeRepository.findById(storeId).orElseThrow(()->new BusinessException(OrderErrorCode.ORDER_CART_EMPTY));
        if(!"OPEN".equals(store.getStatus())){
            throw new BusinessException(OrderErrorCode.ORDER_STORE_CLOSED);
        }
        double distance = DistanceUtils.meters(store.getLat(), store.getLng(), address.getLat(), address.getLng());
        if(distance > store.getDeliveryRadiusM()){
            throw new BusinessException(OrderErrorCode.ORDER_OUT_OF_DELIVERY_AREA);
        }

        // 메뉴 검증 + 금액을 현재 DB 가격으로 다시 계산
        List<MenuEntity> menus = new ArrayList<>();
        List<Integer> quantities = new ArrayList<>();
        int menuAmount = 0;
        for(Map.Entry<String, Integer> entry : cartItems.entrySet()){
            MenuEntity menu = menuRepository.findById(Long.valueOf(entry.getKey()))
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_MENU_UNAVAILABLE));
            if(!menu.getStore().getId().equals(storeId) || !isOrderable(menu)){
                throw new BusinessException(OrderErrorCode.ORDER_MENU_UNAVAILABLE);
            }
            menus.add(menu);
            quantities.add(entry.getValue());
            menuAmount += menu.getPrice() * entry.getValue();
        }
        if (menuAmount < store.getMinOrderAmount()){
            throw new BusinessException(OrderErrorCode.ORDER_MIN_AMOUNT_NOT_MET);
        }

        // 저장 (주소 좌표 메뉴명 단가는 주문 시점 값 복사)
        OrderEntity order = orderRepository.save(OrderEntity.builder()
                    .orderKey(req.getOrderKey())
                    .user(user)
                    .store(store)
                    .menuAmount(menuAmount)
                    .deliveryFee(store.getDeliveryFee())
                    .deliveryAddress(address.getAddress())
                    .deliveryAddressDetail(address.getAddressDetail())
                    .deliveryLat(address.getLat())
                    .deliveryLng(address.getLng())
                    .requestNote(req.getRequestNote())
                    .paymentMethod(req.getPaymentMethod())
                    .build()); 
                    
        for(int i = 0; i < menus.size(); i++){
            MenuEntity menu = menus.get(i);
            orderItemRepository.save(OrderItemEntity.builder()
                        .order(order)
                        .menu(menu)
                        .menuName(menu.getMenuName())
                        .unitPrice(menu.getPrice())
                        .quantity(quantities.get(i))
                        .build());
        }

        confirm(order);
        return order;
    }

    // 접수 대기로 확정 + 커밋 후 알림 예약
    // Sprint 1: 저장 직후 바로 호출 / Spring2: 결제 승인 후 호출하도록 분리
    private void confirm(OrderEntity order){
        // 상태는 생성 시 이미 PENDING. Sprint 2에 '결제대기' 상태가 생기면 여기서 PENDING으로 바꿈
        eventPublisher.publishEvent(OrderEvent.created(order));
    }

    // 주문 가능한 메뉴인지 (가격있음 + 품절-숨김-삭제 아님)
    private boolean isOrderable(MenuEntity menu){
        return menu.getPrice() != null && !menu.isSoldOut() && !menu.isHidden() && !menu.isDeleted();
    }
}
