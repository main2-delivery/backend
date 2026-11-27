package com.main2.delivery.customer.dto.order;

import com.main2.delivery.core.entity.OrderEntity;

import lombok.Getter;

@Getter 
public class OrderCreateResponse {
    private Long orderId;
    private String status;  // PENDING (접수 대기)
    private int menuAmount;
    private int deliveryFee;
    private int totalAmount;

    // Entity를 받아 응답용으로 옮겨 담음
    public OrderCreateResponse(OrderEntity order){
        this.orderId = order.getId();
        this.status = order.getStatus().name();
        this.menuAmount = order.getMenuAmount();
        this.deliveryFee = order.getDeliveryFee();
        this.totalAmount = order.getTotalPrice();
    }
}
