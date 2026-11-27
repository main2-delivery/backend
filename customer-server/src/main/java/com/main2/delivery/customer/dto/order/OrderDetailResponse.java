package com.main2.delivery.customer.dto.order;

import java.time.LocalDateTime;
import java.util.List;

import com.main2.delivery.core.entity.OrderEntity;
import com.main2.delivery.core.entity.OrderItemEntity;

import lombok.Getter;

@Getter 
public class OrderDetailResponse {
    private final Long orderId;
    private final String status;
    private final Long storeId;
    private final String storeName;
    private final List<OrderItemResponse> items;
    private final int menuAmount;
    private final int deliveryFee;
    private final int totalAmount;
    private final String deliveryAddress;
    private final String deliveryAddressDetail;
    private final String requestNote;
    private final String paymentMethod;
    private final String cancelReason;
    // 상태별 시각(아직 안 지났으면 null)
    private final LocalDateTime orderedAt;
    private final LocalDateTime acceptedAt;
    private final LocalDateTime cookedAt;
    private final LocalDateTime completedAt;
    private final LocalDateTime canceledAt;

    public OrderDetailResponse(OrderEntity order, List<OrderItemEntity> items){
        this.orderId = order.getId();
        this.status = order.getStatus().name();
        this.storeId = order.getStore().getId();
        this.storeName = order.getStore().getStoreName();
        this.items = items.stream().map(OrderItemResponse::new).toList();
        this.menuAmount = order.getMenuAmount();
        this.deliveryFee = order.getDeliveryFee();
        this.totalAmount = order.getTotalPrice();
        this.deliveryAddress = order.getDeliveryAddress();
        this.deliveryAddressDetail = order.getDeliveryAddressDetail();
        this.requestNote = order.getRequestNote();
        this.paymentMethod = order.getPaymentMethod();
        this.cancelReason = order.getCancelReason();
        this.orderedAt = order.getCreatedAt();
        this.acceptedAt = order.getAcceptedAt();
        this.cookedAt = order.getCookedAt();
        this.completedAt = order.getCompletedAt();
        this.canceledAt = order.getCanceledAt();
    }
}
