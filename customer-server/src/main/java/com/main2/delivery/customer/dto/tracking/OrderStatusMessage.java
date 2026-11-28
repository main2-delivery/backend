package com.main2.delivery.customer.dto.tracking;

import java.time.LocalDateTime;

import lombok.Getter;

// SSE order-status 이벤트의 data
@Getter 
public class OrderStatusMessage {
    private final Long orderId;
    private final String status;
    private final Integer cookingMinutes;               // 접수 때만 (없으면 null)
    private final LocalDateTime estimatedReadyAt;       // 조리 예상 완료 시각 (없으면 null)  
    private final String reason;                        // 거절/취소 사유 (없으면 null)

    public OrderStatusMessage(Long orderId, String status, Integer cookingMinutes, LocalDateTime estimatedReadyAt, String reason){
        this.orderId = orderId;
        this.status = status;
        this.cookingMinutes = cookingMinutes;
        this.estimatedReadyAt = estimatedReadyAt;
        this.reason = reason;
    }
}
