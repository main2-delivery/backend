package com.main2.delivery.owner.repository.review;


import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.main2.delivery.core.entity.ReviewEntity;

public interface ReviewRepository extends JpaRepository<ReviewEntity, Long>{
    
    @EntityGraph(attributePaths = "user")
    Slice<ReviewEntity> findByStoreId(Long storeId, Pageable pageable);

    @EntityGraph(attributePaths = "user")
    Slice<ReviewEntity> findByStoreIdAndReplyIsNull(Long storeId, Pageable pageable);

    @EntityGraph(attributePaths = {"store", "user"})
    Optional<ReviewEntity> findWithStoreById(Long reviewId);
}
