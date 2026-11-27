package com.main2.delivery.customer.dto.store;

import com.main2.delivery.core.entity.StoreEntity;

import lombok.Getter;
import lombok.Setter;
@Getter
public class StoreListResponse {
    private Long storeId;
    private String storeName;
    private String categoryName;
    private String imageUrl;
    private int distanceM;
    private int minOrderAmount;
    private int deliveryFee;
    private String status;

    @Setter
    private double ratingAvg;
    @Setter
    private long reviewCount;


    // 가게 Entity와 계산한 거리를 응답용으로 옮겨 담음 (평점은 나중에 채움)
    public StoreListResponse(StoreEntity store, int distanceM) {
        this.storeId = store.getId();
        this.storeName = store.getStoreName();
        if( store.getCategory() != null) {
            this.categoryName = store.getCategory().getCategoryName();
        }
        this.imageUrl = store.getImageUrl();
        this.distanceM = distanceM;
        this.minOrderAmount = store.getMinOrderAmount();
        this.deliveryFee = store.getDeliveryFee();
        this.status = store.getStatus();
    }

}
