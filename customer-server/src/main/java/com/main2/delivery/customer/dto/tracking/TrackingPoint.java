package com.main2.delivery.customer.dto.tracking;

import java.math.BigDecimal;

import lombok.Getter;

@Getter 
public class TrackingPoint {
    private final BigDecimal lat;
    private final BigDecimal lng;

    public TrackingPoint(BigDecimal lat, BigDecimal lng){
        this.lat = lat;
        this.lng = lng;
    }
}
