package com.main2.delivery.customer.dto.cart;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@NoArgsConstructor 
public class CartQuantityRequest{
    private int quantity;   // 바꿀 수량 (1~99)
}
