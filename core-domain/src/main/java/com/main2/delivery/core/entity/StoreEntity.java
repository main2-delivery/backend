package com.main2.delivery.core.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "store")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreEntity {

    @Id
    @Column(name = "store_id")
    private Long id;           // 네이버 place_id. 자동 증가 아님

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private UserEntity owner;  // 점주. 수집 가게는 null

    @Column(name = "store_name", nullable = false, length = 100)
    private String storeName;

    @Column(name = "store_phone", length = 20)
    private String storePhone;

    @Column(length = 255)
    private String address;

    @Column(precision = 10, scale = 7)
    private BigDecimal lat;

    @Column(precision = 10, scale = 7)
    private BigDecimal lng;

    @Column(name = "min_order_amount")
    private Integer minOrderAmount;

    @Column(name = "delivery_fee")
    private Integer deliveryFee;

    @Column(name = "is_open")
    private Boolean isOpen;
}