package com.main2.delivery.customer.repository.order;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.main2.delivery.core.entity.OrderEntity;

public interface OrderRepository extends JpaRepository<OrderEntity, Long>{
    // 같은 주문 키로 만든 주문 (있으면 중복 요청)
    Optional<OrderEntity> findByOrderKey(String orderKey);

    // 내 주문 목록 최신순 (가게를 같이 가져와서 N+1 방지)
    @EntityGraph (attributePaths = "store")
    Page<OrderEntity> findByUserIdOrderByIdDesc(Long userId, Pageable pageable);

    // 내 주문 하나 (남의 주문이면 비어 있음)
    Optional<OrderEntity> findByIdAndUserId(Long orderId, Long userId);
}
