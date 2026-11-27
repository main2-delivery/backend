package com.main2.delivery.customer.repository.order;

import org.springframework.data.jpa.repository.JpaRepository;

import com.main2.delivery.core.entity.OrderItemEntity;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long>{
    
}
