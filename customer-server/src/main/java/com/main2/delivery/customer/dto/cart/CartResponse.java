package com.main2.delivery.customer.dto.cart;

import java.util.ArrayList;
import java.util.List;

import com.main2.delivery.core.entity.StoreEntity;

import lombok.Getter;

@Getter
public class CartResponse {

    private Long storeId;
    private String storeName;
    private List<CartItemResponse> items = new ArrayList<>();
    private int menuAmount;      // 메뉴 합계
    private int deliveryFee;     // 배달팁
    private int totalAmount;     // 메뉴 합계 + 배달팁
    private int minOrderAmount;
    private boolean orderable;   // 주문하기 버튼을 켤지 (실제 주문 때 서버가 다시 검사)

    // 빈 장바구니
    public CartResponse() {
    }

    // 가게와 항목들을 받아서 금액·주문 가능 여부 계산
    public CartResponse(StoreEntity store, List<CartItemResponse> items) {
        this.storeId = store.getId();
        this.storeName = store.getStoreName();
        this.items = items;
        this.deliveryFee = store.getDeliveryFee();
        this.minOrderAmount = store.getMinOrderAmount();

        boolean allOrderable = true;
        for (CartItemResponse item : items) {
            this.menuAmount += item.getItemAmount();
            if (!item.isOrderable()) {
                allOrderable = false;
            }
        }
        this.totalAmount = this.menuAmount + this.deliveryFee;
        this.orderable = "OPEN".equals(store.getStatus())
                && !items.isEmpty()
                && allOrderable
                && this.menuAmount >= this.minOrderAmount;
    }
}