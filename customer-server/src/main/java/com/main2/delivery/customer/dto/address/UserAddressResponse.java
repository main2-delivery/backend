package com.main2.delivery.customer.dto.address;

import java.math.BigDecimal;

import com.main2.delivery.core.entity.UserAddressEntity;

import lombok.Getter;

@Getter
public class UserAddressResponse {

    private Long addressId;
    private String label;
    private String address;
    private String addressDetail;
    private BigDecimal lat;
    private BigDecimal lng;
    private boolean defaultAddress;

    // Entity를 받아서 응답용으로 옮겨 담음
    public UserAddressResponse(UserAddressEntity entity) {
        this.addressId = entity.getId();
        this.label = entity.getLabel();
        this.address = entity.getAddress();
        this.addressDetail = entity.getAddressDetail();
        this.lat = entity.getLat();
        this.lng = entity.getLng();
        this.defaultAddress = entity.isDefaultAddress();
    }
}