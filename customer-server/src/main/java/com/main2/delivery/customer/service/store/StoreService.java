package com.main2.delivery.customer.service.store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.main2.delivery.common.exception.BusinessException;
import com.main2.delivery.common.exception.CommonErrorCode;
import com.main2.delivery.core.entity.MenuEntity;
import com.main2.delivery.core.entity.StoreEntity;
import com.main2.delivery.core.entity.UserAddressEntity;
import com.main2.delivery.customer.dto.store.MenuResponse;
import com.main2.delivery.customer.dto.store.StoreDetailResponse;
import com.main2.delivery.customer.dto.store.StoreListResponse;
import com.main2.delivery.customer.repository.address.UserAddressRepository;
import com.main2.delivery.customer.repository.review.ReviewRepository;
import com.main2.delivery.customer.repository.review.StoreRatingSummary;
import com.main2.delivery.customer.repository.store.MenuRepository;
import com.main2.delivery.customer.repository.store.StoreRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoreService {

    // 후보를 찾을 사각형 범위 (미터)
    private static final int SEARCH_RANGE_M = 5000;

    private final StoreRepository storeRepository;
    private final ReviewRepository reviewRepository;
    private final MenuRepository menuRepository;
    private final UserAddressRepository addressRepository;

    // [1] 배달 가능 가게 목록 (내 기본 주소 기준)
    @Transactional(readOnly = true)
    public List<StoreListResponse> findStores(Long userId, Long categoryId, int page, int size) {
        if (page < 0 || size < 1 || size > 50) {
            throw new BusinessException(CommonErrorCode.COMMON_INVALID_INPUT, "page는 0 이상, size는 1~50");
        }
        UserAddressEntity home = addressRepository.findByUserIdAndDefaultAddressTrue(userId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.COMMON_INVALID_INPUT,
                        "기본 배달 주소를 먼저 설정해 주세요"));
        double myLat = home.getLat().doubleValue();
        double myLng = home.getLng().doubleValue();

        // 1. 위경도 사각형으로 후보를 좁힘
        double latRange = SEARCH_RANGE_M / 111000.0;
        double lngRange = SEARCH_RANGE_M / (111000.0 * Math.cos(Math.toRadians(myLat)));
        List<StoreEntity> candidates = storeRepository.findInBox(
                myLat - latRange, myLat + latRange, myLng - lngRange, myLng + lngRange, categoryId);

        // 2. 실제 거리를 계산해서 가게 배달 반경 안만 남김
        List<StoreListResponse> inRange = new ArrayList<>();
        for (StoreEntity store : candidates) {
            double distance = distanceM(myLat, myLng,
                    store.getLat().doubleValue(), store.getLng().doubleValue());
            if (distance <= store.getDeliveryRadiusM()) {
                inRange.add(new StoreListResponse(store, (int) Math.round(distance)));
            }
        }

        // 3. 정렬: 영업 종료(CLOSED)는 아래, 그다음 가까운 순
        inRange.sort((a, b) -> {
            boolean aClosed = a.getStatus().equals("CLOSED");
            boolean bClosed = b.getStatus().equals("CLOSED");
            if (aClosed && !bClosed) {
                return 1;
            }
            if (!aClosed && bClosed) {
                return -1;
            }
            return a.getDistanceM() - b.getDistanceM();
        });

        // 4. 요청한 페이지만큼 잘라냄
        List<StoreListResponse> result = new ArrayList<>();
        int from = page * size;
        int to = Math.min(from + size, inRange.size());
        for (int i = from; i < to; i++) {
            result.add(inRange.get(i));
        }

        // 5. 이 페이지 가게들의 평점·리뷰 수를 한 번에 채움
        if (!result.isEmpty()) {
            List<Long> storeIds = new ArrayList<>();
            for (StoreListResponse item : result) {
                storeIds.add(item.getStoreId());
            }
            Map<Long, StoreRatingSummary> ratingMap = new HashMap<>();
            for (StoreRatingSummary summary : reviewRepository.findRatingSummaries(storeIds)) {
                ratingMap.put(summary.getStoreId(), summary);
            }
            for (StoreListResponse item : result) {
                StoreRatingSummary summary = ratingMap.get(item.getStoreId());
                if (summary != null) {
                    item.setRatingAvg(round1(summary.getRatingAvg()));
                    item.setReviewCount(summary.getReviewCount());
                }
            }
        }
        return result;
    }

    // [2] 가게 상세
    @Transactional(readOnly = true)
    public StoreDetailResponse findStoreDetail(Long storeId) {
        StoreEntity store = findStore(storeId);

        List<Long> storeIds = new ArrayList<>();
        storeIds.add(storeId);
        List<StoreRatingSummary> summaries = reviewRepository.findRatingSummaries(storeIds);

        double ratingAvg = 0.0;
        long reviewCount = 0;
        if (!summaries.isEmpty()) {
            ratingAvg = round1(summaries.get(0).getRatingAvg());
            reviewCount = summaries.get(0).getReviewCount();
        }
        return new StoreDetailResponse(store, ratingAvg, reviewCount);
    }

    // [3] 메뉴판
    @Transactional(readOnly = true)
    public List<MenuResponse> findMenus(Long storeId) {
        findStore(storeId);
        List<MenuResponse> result = new ArrayList<>();
        for (MenuEntity menu : menuRepository.findMenuBoard(storeId)) {
            result.add(new MenuResponse(menu));
        }
        return result;
    }

    // 가게 한 건 (없으면 404)
    private StoreEntity findStore(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.COMMON_NOT_FOUND, "가게 없음"));
    }

    // 소수 첫째 자리 반올림 (null이면 0.0)
    private double round1(Double avg) {
        if (avg == null) {
            return 0.0;
        }
        return Math.round(avg * 10) / 10.0;
    }

    // 두 위경도 사이 거리 (미터, 하버사인 공식)
    private double distanceM(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return earthRadius * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}