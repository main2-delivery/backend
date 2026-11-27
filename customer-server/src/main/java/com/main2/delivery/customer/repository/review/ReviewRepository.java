package com.main2.delivery.customer.repository.review;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.main2.delivery.core.entity.ReviewEntity;

import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<ReviewEntity , Long>{

    // 여러 가게의 평균 별점과 리뷰 수를 한 번에 조회
    @Query(value = "select store_id as storeId, avg(rating) as ratingAvg, count(*) as reviewCount "
            + "from review where store_id in (:storeIds) group by store_id", nativeQuery = true)
    List<StoreRatingSummary> findRatingSummaries(@Param("storeIds") List<Long> storeIds);

}