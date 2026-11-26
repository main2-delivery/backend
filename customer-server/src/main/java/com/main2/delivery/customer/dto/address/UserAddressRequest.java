package com.main2.delivery.customer.dto.address;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserAddressRequest {

    private String label;           // 별칭 (집, 회사)
    private String address;         // 기본 주소
    private String addressDetail;   // 상세 주소
    private BigDecimal lat;         // 위도
    private BigDecimal lng;         // 경도
}