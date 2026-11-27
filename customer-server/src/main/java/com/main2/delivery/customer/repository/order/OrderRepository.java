package com.main2.delivery.customer.repository.order;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.main2.delivery.core.entity.OrderEntity;

public interface OrderRepository extends JpaRepository<OrderEntity, Long>{
    // 같은 주문 키로 만든 주문 (있으면 중복 요청)
    Optional<OrderEntity> findByOrderKey(String orderKey);
}
