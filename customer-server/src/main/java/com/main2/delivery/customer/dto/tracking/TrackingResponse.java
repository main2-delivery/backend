package com.main2.delivery.customer.dto.tracking;

import java.time.LocalDateTime;

import com.main2.delivery.core.entity.OrderEntity;

import lombok.Getter;

// robot·remainingMinutes·pin은 Sprint 2(배차)에 채움. 자리를 미리 둬서 앱 화면을 다시 안 고치게
@Getter 
public class TrackingResponse {
    private final Long orderId;
    private final String status;
    private final TrackingPoint store;              // 가게 좌표
    private final TrackingPoint destination;        // 배달지 좌표 (주문 시점)
    private final TrackingPoint robot;              // Sprint2
    private final LocalDateTime estimatedReadyAt;
    private final Integer remainingMinutes;  // Sprint2
    private final String pin;                       // Sprint2

    public TrackingResponse(OrderEntity order, LocalDateTime estimatedReadyAt){
        this.orderId = order.getId();
        this.status = order.getStatus().name();
        this.store = new TrackingPoint(order.getStore().getLat(), order.getStore().getLng());
        this.destination = new TrackingPoint(order.getDeliveryLat(), order.getDeliveryLng());
        this.robot = null;
        this.estimatedReadyAt = estimatedReadyAt;
        this.remainingMinutes = null;
        this.pin = null;
    }
}
