package com.main2.delivery.owner.service.review;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.main2.delivery.common.exception.BusinessException;
import com.main2.delivery.common.exception.CommonErrorCode;
import com.main2.delivery.core.entity.OwnerEntity;
import com.main2.delivery.core.entity.ReviewEntity;
import com.main2.delivery.core.entity.StoreEntity;
import com.main2.delivery.owner.dto.review.ReviewPageResponse;
import com.main2.delivery.owner.dto.review.ReviewReplyRequest;
import com.main2.delivery.owner.dto.review.ReviewResponse;
import com.main2.delivery.owner.repository.review.ReviewRepository;
import com.main2.delivery.owner.repository.store.StoreRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final StoreRepository storeRepository;

    public ReviewPageResponse findReviews(Long ownerId, Long storeId, boolean unanswered, Pageable pageable) {
    findMyStore(ownerId, storeId);

    Slice<ReviewEntity> result = unanswered
            ? reviewRepository.findByStoreIdAndReplyIsNull(storeId, pageable)
            : reviewRepository.findByStoreId(storeId, pageable);

    List<ReviewResponse> reviews = result.getContent().stream()
            .map(ReviewResponse::new)
            .toList();

    return new ReviewPageResponse(reviews, result.getNumber(), result.hasNext());
    }

    @Transactional
    public ReviewResponse putReply(Long ownerId, Long reviewId, ReviewReplyRequest req){
        ReviewEntity reviewEntity = findMyReview(ownerId, reviewId);
        reviewEntity.setReply(req.getReply());

        return new ReviewResponse(reviewEntity);
    }

    @Transactional 
    public void deleteReply(Long ownerId, Long reviewId){
        ReviewEntity reviewEntity = findMyReview(ownerId, reviewId);
        reviewEntity.setReply(null);
        reviewEntity.setRepliedAt(null);
    }


    // 내 가게 찾기: 없으면 404, 남의 가게면 403
    private StoreEntity findMyStore(Long ownerId, Long storeId) {
        StoreEntity store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.COMMON_NOT_FOUND, "가게를 찾을 수 없습니다."));
        checkOwner(store, ownerId);
        return store;
    }

    // 수집 가게(owner = null)는 누구의 가게도 아님
    private void checkOwner(StoreEntity store, Long ownerId) {
        OwnerEntity owner = store.getOwner();
        if (owner == null || !owner.getId().equals(ownerId)) {
            throw new BusinessException(CommonErrorCode.COMMON_FORBIDDEN, "내 가게가 아닙니다.");
        }
    }

    // 내 가게 리뷰 찾기: 없으면 404, 남의 가게 리뷰면 403
    private ReviewEntity findMyReview(Long ownerId, Long reviewId) {
        ReviewEntity review = reviewRepository.findWithStoreById(reviewId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.COMMON_NOT_FOUND, "리뷰를 찾을 수 없습니다."));
        checkOwner(review.getStore(), ownerId);   // 이미 있는 메서드 재사용
        return review;
    }
}
