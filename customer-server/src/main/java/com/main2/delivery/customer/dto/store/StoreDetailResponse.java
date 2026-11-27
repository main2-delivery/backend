package com.main2.delivery.customer.dto.store;

import java.time.LocalTime;

import com.main2.delivery.core.entity.StoreEntity;

import lombok.Getter;
@Getter 
public class StoreDetailResponse {
    private Long storeId;
    private String storeName;
    private String imageUrl;
    private int minOrderAmount;
    private int deliveryFee;
    private LocalTime openTime;
    private LocalTime closeTime;
    private String status;
    private double ratingAvg;
    private long reviewCount;


    // 가게 Entity에 평점, 리뷰 수를 더해서 응답용으로 옮겨 담음
    public StoreDetailResponse(StoreEntity store, double ratingAvg, long reviewCount) {
        this.storeId = store.getId();
        this.storeName = store.getStoreName();
        this.imageUrl = store.getImageUrl();
        this.minOrderAmount = store.getMinOrderAmount();
        this.deliveryFee = store.getDeliveryFee();
        this.openTime = store.getOpenTime();
        this.closeTime = store.getCloseTime();
        this.status = store.getStatus();
        this.ratingAvg = ratingAvg;
        this.reviewCount = reviewCount;
    }
}
