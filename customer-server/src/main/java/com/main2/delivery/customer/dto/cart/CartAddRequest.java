package com.main2.delivery.customer.dto.cart;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@NoArgsConstructor 
public class CartAddRequest {
    private Long menuId;            // 담을 메뉴
    private int quantity;           // 담을 수량(1~99)
    private boolean clearExisting;   // 다른 가게 메뉴가 있으면 비우고 담을지
}