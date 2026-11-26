package com.main2.delivery.core.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private StoreCategoryEntity category;   // 분류 전에는 null

    @Setter
    @Column(name = "store_name", nullable = false, length = 100)
    private String storeName;

    @Setter
    @Column(name = "store_phone", length = 20)
    private String storePhone;

    @Setter
    @Column(name = "business_number", length = 12)
    private String businessNumber;   // 000-00-00000 형식만 검사

    @Setter
    @Column(nullable = false, length = 255)
    private String address;

    @Setter
    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal lat;

    @Setter
    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal lng;

    @Setter
    @Column(name = "min_order_amount", nullable = false)
    private int minOrderAmount;

    @Setter
    @Column(name = "delivery_fee", nullable = false)
    private int deliveryFee;

    @Setter
    @Column(name = "delivery_radius_m", nullable = false)
    private int deliveryRadiusM;     // 배달 가능 반경(미터)

    @Setter
    @Column(name = "open_time")
    private LocalTime openTime;

    @Setter
    @Column(name = "close_time")
    private LocalTime closeTime;

    @Setter
    @Column(nullable = false, length = 20)
    private String status;         // OPEN / PREPARING / PAUSED / CLOSED (기본값 OPEN 은 DB 가 넣음)

    @Setter
    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    // 생성은 빌더로만 (StoreEntity.builder()...build()). status 는 넣지 않음 → DB 기본값 OPEN
    @Builder
    private StoreEntity(
            OwnerEntity owner,
            StoreCategoryEntity category,
            String storeName,
            String storePhone,
            String businessNumber,
            String address,
            BigDecimal lat,
            BigDecimal lng,
            int minOrderAmount,
            int deliveryFee,
            int deliveryRadiusM,
            LocalTime openTime,
            LocalTime closeTime,
            String imageUrl
    ) {
        this.owner = owner;
        this.category = category;
        this.storeName = storeName;
        this.storePhone = storePhone;
        this.businessNumber = businessNumber;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.minOrderAmount = minOrderAmount;
        this.deliveryFee = deliveryFee;
        this.deliveryRadiusM = deliveryRadiusM > 0 ? deliveryRadiusM : 3000;   // 안 넣으면 DB 기본값과 같은 3000m
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.imageUrl = imageUrl;
    }
}
