package com.main2.delivery.core.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@DynamicInsert   // status 를 비워 두면 DB 기본값 OPEN 이 들어감
@Table(name = "store")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreEntity extends BaseTimeEntity {

    // 수집 가게: 네이버 place_id (load_db.py 가 직접 넣음)
    // 입점 가게: AUTO_INCREMENT 로 자동 발급
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private OwnerEntity owner;              // 수집 가게는 null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private StoreCategoryEntity category;   // 분류 전에는 null

    @Column(name = "store_name", nullable = false, length = 100)
    private String storeName;

    @Column(name = "store_phone", length = 20)
    private String storePhone;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal lat;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal lng;

    @Column(name = "min_order_amount", nullable = false)
    private int minOrderAmount;

    @Column(name = "delivery_fee", nullable = false)
    private int deliveryFee;

    @Column(name = "open_time")
    private LocalTime openTime;

    @Column(name = "close_time")
    private LocalTime closeTime;

    @Column(nullable = false, length = 20)
    private String status;         // OPEN / PREPARING / PAUSED / CLOSED (기본값 OPEN 은 DB 가 넣음)

    @Column(name = "image_url", length = 1000)
    private String imageUrl;
}
