package com.main2.delivery.customer.dto.order;

import java.time.LocalDateTime;
import java.util.List;

import com.main2.delivery.core.entity.OrderEntity;
import com.main2.delivery.core.entity.OrderItemEntity;

import lombok.Getter;

@Getter 
public class OrderSummaryResponse {
    private final Long orderId;
    private final Long storeId;
    private final String storeName;
    private final String status;
    private final String menuSummary;   // "후라이드 외 2개"
    private final int totalAmount;
    private final LocalDateTime orderedAt;

    public OrderSummaryResponse(OrderEntity order, List<OrderItemEntity> items){
        this.orderId = order.getId();
        this.storeId = order.getStore().getId();
        this.storeName = order.getStore().getStoreName();
        this.status = order.getStatus().name();
        this.menuSummary = summary(items);
        this.totalAmount = order.getTotalPrice();
        this.orderedAt = order.getCreatedAt();
    }

    // 첫 메뉴 이름 + 나머지 개수
    private static String summary(List<OrderItemEntity> items){
        if(items.isEmpty()){
            return "";
        }
        String first = items.get(0).getMenuName();
        return items.size() == 1 ? first : first + " 외 " + (items.size()-1) + "개";
    }
}
