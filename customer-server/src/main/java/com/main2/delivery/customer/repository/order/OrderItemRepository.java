package com.main2.delivery.customer.repository.order;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.main2.delivery.core.entity.OrderItemEntity;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long>{
    // 주문 하나의 메뉴들 (상세)
    List<OrderItemEntity> findByOrderId(Long orderId);

    // 여러 주문의 메뉴를 IN 쿼리 한번으로 (목록 N+1 방지)
    List<OrderItemEntity> findByOrderIdIn(List<Long> orderIds);
}
