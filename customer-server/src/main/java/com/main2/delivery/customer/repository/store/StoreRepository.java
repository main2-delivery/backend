package com.main2.delivery.customer.repository.store;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.main2.delivery.core.entity.StoreEntity;

import org.springframework.data.repository.query.Param;

public interface StoreRepository extends JpaRepository<StoreEntity , Long> {
    // 위경도 사각형 안의 가게 (카테고리가 null이면 전체)
    @Query(value = "select * from store "
        + "where lat between :minLat and :maxLat "
        + "and lng between :minLng and :maxLng "
        + "and (:categoryId is null or category_id = :categoryId)", nativeQuery = true)
    List<StoreEntity> findInBox(@Param("minLat") double minLat , @Param("maxLat") double maxLat,
                                @Param("minLng") double minLng, @Param("maxLng") double maxLng,
                                @Param("categoryId") Long categoryId);
}