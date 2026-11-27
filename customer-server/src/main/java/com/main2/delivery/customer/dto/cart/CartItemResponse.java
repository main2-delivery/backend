package com.main2.delivery.customer.dto.cart;

import com.main2.delivery.core.entity.MenuEntity;

import lombok.Getter;

@Getter
public class CartItemResponse {

    private String cartItemId;   // 장바구니 항목 번호 (지금은 메뉴 번호, 옵션 생기면 확장)
    private Long menuId;
    private String menuName;
    private int unitPrice;       // 현재 DB 가격
    private int quantity;
    private int itemAmount;      // unitPrice × quantity
    private boolean orderable;   // 품절·숨김·삭제·가격 없음이면 false

    // Entity와 수량을 받아서 응답용으로 옮겨 담음
    public CartItemResponse(String cartItemId, MenuEntity menu, int quantity) {
        this.cartItemId = cartItemId;
        this.menuId = menu.getId();
        this.menuName = menu.getMenuName();
        this.unitPrice = menu.getPrice() == null ? 0 : menu.getPrice();
        this.quantity = quantity;
        this.itemAmount = this.unitPrice * quantity;
        this.orderable = menu.getPrice() != null
                && !menu.isSoldOut()
                && !menu.isHidden()
                && !menu.isDeleted();
    }
}