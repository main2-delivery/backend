package com.main2.delivery.customer.dto.order;

import com.main2.delivery.core.entity.OrderItemEntity;

import lombok.Getter;

@Getter 
public class OrderItemResponse {
    private final String menuName;  // 주문 시점 이름
    private final int unitPrice;    // 주문 시점 가격 (메뉴 가격이 바뀌어도 그대로)
    private final int quantity;
    private final int amount;       // 단가 * 수량

    public OrderItemResponse(OrderItemEntity item){
        this.menuName = item.getMenuName();
        this.unitPrice = item.getUnitPrice();
        this.quantity = item.getQuantity();
        this.amount = item.getUnitPrice() * item.getQuantity();
    }
}
