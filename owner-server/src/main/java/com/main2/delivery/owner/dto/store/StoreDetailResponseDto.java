package com.main2.delivery.owner.dto.store;

import com.main2.delivery.core.entity.StoreEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

// 가게 정보 조회·수정 응답
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreDetailResponseDto {
    private Long storeId;
    private String name;
    private Long categoryId;
    private String categoryName;
    private String phone;
    private String businessNumber;
    private String address;
    private BigDecimal lat;
    private BigDecimal lng;
    private int minOrderAmount;
    private int deliveryFee;
    private int deliveryRadiusM;
    private LocalTime openTime;
    private LocalTime closeTime;
    private String status;
    private String logoUrl;

    public static StoreDetailResponseDto from(StoreEntity store) {
        boolean hasCategory = store.getCategory() != null;
        return StoreDetailResponseDto.builder()
                .storeId(store.getId())
                .name(store.getStoreName())
                .categoryId(hasCategory ? store.getCategory().getId() : null)
                .categoryName(hasCategory ? store.getCategory().getCategoryName() : null)
                .phone(store.getStorePhone())
                .businessNumber(store.getBusinessNumber())
                .address(store.getAddress())
                .lat(store.getLat())
                .lng(store.getLng())
                .minOrderAmount(store.getMinOrderAmount())
                .deliveryFee(store.getDeliveryFee())
                .deliveryRadiusM(store.getDeliveryRadiusM())
                .openTime(store.getOpenTime())
                .closeTime(store.getCloseTime())
                .status(store.getStatus())
                .logoUrl(store.getImageUrl())
                .build();
    }
}
