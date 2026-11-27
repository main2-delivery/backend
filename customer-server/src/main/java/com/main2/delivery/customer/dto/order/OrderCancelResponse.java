package com.main2.delivery.customer.dto.order;

import java.time.LocalDateTime;

import com.main2.delivery.core.entity.OrderEntity;

import lombok.Getter;

@Getter
public class OrderCancelResponse {
    private final Long orderId;
    private final String status;
    private final String cancelReason;
    private final LocalDateTime canceledAt;

    public OrderCancelResponse(OrderEntity order){
        this.orderId = order.getId();
        this.status = order.getStatus().name();
        this.cancelReason = order.getCancelReason();
        this.canceledAt = order.getCanceledAt();
    }
}
