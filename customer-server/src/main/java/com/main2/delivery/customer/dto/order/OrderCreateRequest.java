package com.main2.delivery.customer.dto.order;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@NoArgsConstructor 
public class OrderCreateRequest {
    private String orderKey;    // 앱이 주문서 진입 시 만든 값 (재시도 때 같은 값)
    private Long addressId;     // 배달 주소
    private String requestNote; // 요청사항
    private String paymentMethod;   // CARD / EASY_PAY  
}
