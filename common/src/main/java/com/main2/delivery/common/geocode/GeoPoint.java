package com.main2.delivery.common.geocode;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

// 위도(lat)·경도(lng) 한 쌍
@Getter
@AllArgsConstructor
public class GeoPoint {
    private final BigDecimal lat;
    private final BigDecimal lng;
}
