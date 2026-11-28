package com.main2.delivery.owner.service.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;

import com.main2.delivery.common.exception.BusinessException;
import com.main2.delivery.common.exception.CommonErrorCode;
import com.main2.delivery.core.entity.OwnerEntity;
import com.main2.delivery.core.entity.ReviewEntity;
import com.main2.delivery.core.entity.StoreEntity;
import com.main2.delivery.core.entity.UserEntity;
import com.main2.delivery.owner.dto.review.ReviewPageResponse;
import com.main2.delivery.owner.dto.review.ReviewReplyRequest;
import com.main2.delivery.owner.dto.review.ReviewResponse;
import com.main2.delivery.owner.repository.review.ReviewRepository;
import com.main2.delivery.owner.repository.store.StoreRepository;

/**
 * 리뷰 서비스 단위 테스트.
 * DB 대신 가짜 Repository(@Mock)를 써서, 로그인(JWT) 없이 ownerId만 넘겨 확인한다.
 */
@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    ReviewRepository reviewRepository;

    @Mock
    StoreRepository storeRepository;

    @InjectMocks
    ReviewService reviewService;

    static final Long MY_OWNER_ID = 17L;
    static final Long OTHER_OWNER_ID = 99L;
    static final Long MY_STORE_ID = 10L;
    static final Long COLLECTED_STORE_ID = 5L;
    static final Long MY_REVIEW_ID = 201L;
    static final Long COLLECTED_REVIEW_ID = 202L;

    final Pageable pageable = PageRequest.of(0, 20);

    StoreEntity myStore;
    StoreEntity collectedStore;
    ReviewEntity myReview;          // 주문 리뷰 (작성자 있음)
    ReviewEntity noUserReview;      // 작성자 없는 리뷰

    @BeforeEach
    void setUp() {
        OwnerEntity me = OwnerEntity.builder()
                .phone("01011112222")
                .passwordHash("test-hash")
                .name("테스트업주")
                .build();
        setId(me, MY_OWNER_ID);

        myStore = StoreEntity.builder()
                .owner(me)
                .storeName("테스트치킨")
                .build();
        setId(myStore, MY_STORE_ID);

        collectedStore = StoreEntity.builder()
                .owner(null)                    // 수집 가게는 주인이 없음
                .storeName("수집가게")
                .build();
        setId(collectedStore, COLLECTED_STORE_ID);

        UserEntity customer = UserEntity.builder()
                .loginId("customer1")
                .nickname("배고픈곰")
                .build();

        myReview = ReviewEntity.builder()
                .store(myStore)
                .user(customer)
                .rating(new BigDecimal("4.5"))
                .body("맛있어요")
                .build();
        setId(myReview, MY_REVIEW_ID);

        noUserReview = ReviewEntity.builder()
                .store(collectedStore)
                .user(null)
                .body("수집된 리뷰")
                .build();
        setId(noUserReview, COLLECTED_REVIEW_ID);
    }

    @Nested
    @DisplayName("리뷰 목록")
    class FindReviews {

        @Test
        void 전체_리뷰를_돌려주고_페이지_정보를_담는다() {
            given(storeRepository.findById(MY_STORE_ID)).willReturn(Optional.of(myStore));
            given(reviewRepository.findByStoreId(MY_STORE_ID, pageable))
                    .willReturn(new SliceImpl<>(List.of(myReview), pageable, true));

            ReviewPageResponse result = reviewService.findReviews(MY_OWNER_ID, MY_STORE_ID, false, pageable);

            assertThat(result.getReviews()).hasSize(1);
            ReviewResponse review = result.getReviews().get(0);
            assertThat(review.getReviewId()).isEqualTo(MY_REVIEW_ID);
            assertThat(review.getNickname()).isEqualTo("배고픈곰");
            assertThat(review.getContent()).isEqualTo("맛있어요");
            assertThat(result.getPage()).isZero();
            assertThat(result.isHasNext()).isTrue();
            verify(reviewRepository, never()).findByStoreIdAndReplyIsNull(any(), any());
        }

        @Test
        void unanswered가_true면_미답변_조회를_쓴다() {
            given(storeRepository.findById(MY_STORE_ID)).willReturn(Optional.of(myStore));
            given(reviewRepository.findByStoreIdAndReplyIsNull(MY_STORE_ID, pageable))
                    .willReturn(new SliceImpl<>(List.of(myReview), pageable, false));

            ReviewPageResponse result = reviewService.findReviews(MY_OWNER_ID, MY_STORE_ID, true, pageable);

            assertThat(result.getReviews()).hasSize(1);
            assertThat(result.isHasNext()).isFalse();
            verify(reviewRepository, never()).findByStoreId(any(), any());
        }

        @Test
        void 작성자가_없는_리뷰는_닉네임이_익명() {
            // 수집 리뷰처럼 user가 없는 리뷰가 섞여도 목록 조회가 터지면 안 된다
            given(storeRepository.findById(MY_STORE_ID)).willReturn(Optional.of(myStore));
            given(reviewRepository.findByStoreId(MY_STORE_ID, pageable))
                    .willReturn(new SliceImpl<>(List.of(noUserReview), pageable, false));

            ReviewPageResponse result = reviewService.findReviews(MY_OWNER_ID, MY_STORE_ID, false, pageable);

            assertThat(result.getReviews().get(0).getNickname()).isEqualTo("익명");
        }

        @Test
        void 다른_업주의_가게면_403() {
            given(storeRepository.findById(MY_STORE_ID)).willReturn(Optional.of(myStore));

            assertErrorCode(() -> reviewService.findReviews(OTHER_OWNER_ID, MY_STORE_ID, false, pageable),
                    CommonErrorCode.COMMON_FORBIDDEN);
            verify(reviewRepository, never()).findByStoreId(any(), any());
        }

        @Test
        void 수집_가게면_403() {
            given(storeRepository.findById(COLLECTED_STORE_ID)).willReturn(Optional.of(collectedStore));

            assertErrorCode(() -> reviewService.findReviews(MY_OWNER_ID, COLLECTED_STORE_ID, false, pageable),
                    CommonErrorCode.COMMON_FORBIDDEN);
        }

        @Test
        void 없는_가게면_404() {
            given(storeRepository.findById(MY_STORE_ID)).willReturn(Optional.empty());

            assertErrorCode(() -> reviewService.findReviews(MY_OWNER_ID, MY_STORE_ID, false, pageable),
                    CommonErrorCode.COMMON_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("답글 작성·수정")
    class PutReply {

        @Test
        void 답글과_답글_시각을_저장한다() {
            given(reviewRepository.findWithStoreById(MY_REVIEW_ID)).willReturn(Optional.of(myReview));

            ReviewResponse result = reviewService.putReply(MY_OWNER_ID, MY_REVIEW_ID, reply("감사합니다"));

            assertThat(myReview.getReply()).isEqualTo("감사합니다");
            assertThat(myReview.getRepliedAt()).isNotNull();
            assertThat(result.getReply()).isEqualTo("감사합니다");
            assertThat(result.getRepliedAt()).isNotNull();
        }

        @Test
        void 이미_답글이_있으면_덮어쓴다() {
            myReview.setReply("예전 답글");
            given(reviewRepository.findWithStoreById(MY_REVIEW_ID)).willReturn(Optional.of(myReview));

            reviewService.putReply(MY_OWNER_ID, MY_REVIEW_ID, reply("새 답글"));

            assertThat(myReview.getReply()).isEqualTo("새 답글");
        }

        @Test
        void 다른_업주의_리뷰면_403이고_바뀌지_않는다() {
            given(reviewRepository.findWithStoreById(MY_REVIEW_ID)).willReturn(Optional.of(myReview));

            assertErrorCode(() -> reviewService.putReply(OTHER_OWNER_ID, MY_REVIEW_ID, reply("남의 답글")),
                    CommonErrorCode.COMMON_FORBIDDEN);
            assertThat(myReview.getReply()).isNull();
        }

        @Test
        void 수집_가게의_리뷰면_403() {
            given(reviewRepository.findWithStoreById(COLLECTED_REVIEW_ID)).willReturn(Optional.of(noUserReview));

            assertErrorCode(() -> reviewService.putReply(MY_OWNER_ID, COLLECTED_REVIEW_ID, reply("답글")),
                    CommonErrorCode.COMMON_FORBIDDEN);
        }

        @Test
        void 없는_리뷰면_404() {
            given(reviewRepository.findWithStoreById(MY_REVIEW_ID)).willReturn(Optional.empty());

            assertErrorCode(() -> reviewService.putReply(MY_OWNER_ID, MY_REVIEW_ID, reply("답글")),
                    CommonErrorCode.COMMON_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("답글 삭제")
    class DeleteReply {

        @Test
        void 답글과_답글_시각을_지운다() {
            myReview.setReply("감사합니다");
            myReview.setRepliedAt(java.time.LocalDateTime.now());
            given(reviewRepository.findWithStoreById(MY_REVIEW_ID)).willReturn(Optional.of(myReview));

            reviewService.deleteReply(MY_OWNER_ID, MY_REVIEW_ID);

            assertThat(myReview.getReply()).isNull();
            assertThat(myReview.getRepliedAt()).isNull();
        }

        @Test
        void 다른_업주의_리뷰면_403이고_답글이_남는다() {
            myReview.setReply("감사합니다");
            given(reviewRepository.findWithStoreById(MY_REVIEW_ID)).willReturn(Optional.of(myReview));

            assertErrorCode(() -> reviewService.deleteReply(OTHER_OWNER_ID, MY_REVIEW_ID),
                    CommonErrorCode.COMMON_FORBIDDEN);
            assertThat(myReview.getReply()).isEqualTo("감사합니다");
        }

        @Test
        void 없는_리뷰면_404() {
            given(reviewRepository.findWithStoreById(MY_REVIEW_ID)).willReturn(Optional.empty());

            assertErrorCode(() -> reviewService.deleteReply(MY_OWNER_ID, MY_REVIEW_ID),
                    CommonErrorCode.COMMON_NOT_FOUND);
        }
    }

    private static ReviewReplyRequest reply(String text) {
        ReviewReplyRequest req = new ReviewReplyRequest();
        req.setReply(text);
        return req;
    }

    // 엔티티 id는 Setter가 없고 DB가 정해 주므로, 테스트에서만 직접 넣는다
    private static void setId(Object entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
    }

    private static void assertErrorCode(Runnable action, CommonErrorCode expected) {
        assertThatThrownBy(action::run)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(expected);
    }
}
