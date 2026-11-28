package com.main2.delivery.owner.service.store;

import com.main2.delivery.common.exception.BusinessException;
import com.main2.delivery.common.geocode.GeoCodingService;
import com.main2.delivery.common.geocode.GeoPoint;
import com.main2.delivery.core.entity.OwnerEntity;
import com.main2.delivery.core.entity.StoreCategoryEntity;
import com.main2.delivery.core.entity.StoreEntity;
import com.main2.delivery.owner.dto.store.StoreCreateRequestDto;
import com.main2.delivery.owner.dto.store.StoreCreateResponseDto;
import com.main2.delivery.owner.dto.store.StoreDetailResponseDto;
import com.main2.delivery.owner.dto.store.StoreStatus;
import com.main2.delivery.owner.dto.store.StoreStatusRequestDto;
import com.main2.delivery.owner.dto.store.StoreStatusResponseDto;
import com.main2.delivery.owner.dto.store.StoreSummaryResponseDto;
import com.main2.delivery.owner.dto.store.StoreUpdateRequestDto;
import com.main2.delivery.owner.exception.OwnerErrorCode;
import com.main2.delivery.owner.repository.owner.OwnerRepository;
import com.main2.delivery.owner.repository.store.StoreCategoryRepository;
import com.main2.delivery.owner.repository.store.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {

    private static final String PHONE_PATTERN = "\\d{2,4}-\\d{3,4}-\\d{4}";
    private static final String BUSINESS_NUMBER_PATTERN = "\\d{3}-\\d{2}-\\d{5}";

    private final StoreRepository storeRepository;
    private final StoreCategoryRepository storeCategoryRepository;
    private final OwnerRepository ownerRepository;
    private final GeoCodingService geoCodingService;

    // 1. 입점 신청: 주소를 좌표로 못 바꾸면 가게를 만들지 않는다
    @Transactional
    public StoreCreateResponseDto apply(Long ownerId, StoreCreateRequestDto request) {
        validateCreate(request);
        OwnerEntity owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new BusinessException(OwnerErrorCode.OWNER_NOT_FOUND));
        StoreCategoryEntity category = null;
        if (request.getCategoryId() != null) {
            category = storeCategoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new BusinessException(OwnerErrorCode.STORE_CATEGORY_NOT_FOUND));
        }
        GeoPoint point = geoCodingService.geocode(request.getAddress())
                .orElseThrow(() -> new BusinessException(OwnerErrorCode.STORE_GEOCODING_FAILED));

        StoreEntity store = StoreEntity.builder()
                .owner(owner)
                .category(category)
                .storeName(request.getName().trim())
                .storePhone(request.getPhone())
                .businessNumber(request.getBusinessNumber())
                .address(request.getAddress().trim())
                .lat(point.getLat())
                .lng(point.getLng())
                .build();
        store.setStatus("PREPARING");
        StoreEntity saved = storeRepository.save(store);
        return StoreCreateResponseDto.from(saved);
    }

    // 2. 내 가게 목록
    public List<StoreSummaryResponseDto> getMyStores(Long ownerId) {
        List<StoreEntity> stores = storeRepository.findAllByOwnerIdOrderByIdAsc(ownerId);
        List<StoreSummaryResponseDto> result = new ArrayList<>();
        stores.forEach(store -> result.add(StoreSummaryResponseDto.from(store)));
        return result;
    }

    // 3. 가게 정보 조회
    public StoreDetailResponseDto getMyStore(Long ownerId, Long storeId) {
        return StoreDetailResponseDto.from(findMyStore(ownerId, storeId));
    }

    // 4. 가게 정보 수정: 엔티티 값만 바꾸면 트랜잭션이 끝날 때 UPDATE 된다
    @Transactional
    public StoreDetailResponseDto updateMyStore(Long ownerId, Long storeId, StoreUpdateRequestDto request) {
        validateUpdate(request);
        StoreEntity store = findMyStore(ownerId, storeId);
        store.setStorePhone(request.getPhone());
        store.setOpenTime(request.getOpenTime());
        store.setCloseTime(request.getCloseTime());
        store.setMinOrderAmount(request.getMinOrderAmount());
        store.setDeliveryFee(request.getDeliveryFee());
        store.setDeliveryRadiusM(request.getDeliveryRadiusM());
        store.setImageUrl(request.getLogoUrl());
        return StoreDetailResponseDto.from(store);
    }

    // 5. 영업 상태 변경: 고객 서버가 같은 DB 를 읽으므로 커밋되면 가게 목록·상세·주문 가능 여부에 바로 반영된다
    @Transactional
    public StoreStatusResponseDto changeStatus(Long ownerId, Long storeId, StoreStatusRequestDto request) {
        StoreStatus next = StoreStatus.from(request == null ? null : request.getStatus())
                .orElseThrow(() -> new BusinessException(OwnerErrorCode.STORE_INVALID_STATUS));
        StoreEntity store = findMyStore(ownerId, storeId);
        // 영업시간 없이 문을 열면 고객 앱에 영업시간이 비어 보이므로 OPEN 전에 입력을 요구한다
        if (next == StoreStatus.OPEN && (store.getOpenTime() == null || store.getCloseTime() == null)) {
            throw new BusinessException(OwnerErrorCode.STORE_OPEN_REQUIREMENTS_NOT_MET);
        }
        store.setStatus(next.name());   // 같은 상태로 다시 요청해도 그대로 성공 (멱등)
        return StoreStatusResponseDto.from(store);
    }

    // 없는 가게는 404, 다른 업주의 가게는 403
    private StoreEntity findMyStore(Long ownerId, Long storeId) {
        StoreEntity store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(OwnerErrorCode.STORE_NOT_FOUND));
        if (store.getOwner() == null || !store.getOwner().getId().equals(ownerId)) {
            throw new BusinessException(OwnerErrorCode.STORE_NOT_OWNER);
        }
        return store;
    }

    // 입점 신청 입력값 검사
    private void validateCreate(StoreCreateRequestDto request) {
        if (isBlank(request.getName()) || request.getName().trim().length() > 100) {
            throw new BusinessException(OwnerErrorCode.STORE_INVALID_NAME);
        }
        if (isBlank(request.getAddress()) || request.getAddress().trim().length() > 255) {
            throw new BusinessException(OwnerErrorCode.STORE_INVALID_ADDRESS);
        }
        if (request.getPhone() == null || !request.getPhone().matches(PHONE_PATTERN)) {
            throw new BusinessException(OwnerErrorCode.STORE_INVALID_PHONE);
        }
        if (request.getBusinessNumber() == null || !request.getBusinessNumber().matches(BUSINESS_NUMBER_PATTERN)) {
            throw new BusinessException(OwnerErrorCode.STORE_INVALID_BUSINESS_NUMBER);
        }
    }

    // 가게 정보 수정 입력값 검사 (영업시간은 둘 다 있거나 둘 다 없고, 같은 시각이면 안 된다)
    private void validateUpdate(StoreUpdateRequestDto request) {
        if (request.getPhone() == null || !request.getPhone().matches(PHONE_PATTERN)) {
            throw new BusinessException(OwnerErrorCode.STORE_INVALID_PHONE);
        }
        if (request.getMinOrderAmount() == null || request.getMinOrderAmount() < 0
                || request.getDeliveryFee() == null || request.getDeliveryFee() < 0) {
            throw new BusinessException(OwnerErrorCode.STORE_INVALID_AMOUNT);
        }
        if (request.getDeliveryRadiusM() == null || request.getDeliveryRadiusM() <= 0) {
            throw new BusinessException(OwnerErrorCode.STORE_INVALID_DELIVERY_RADIUS);
        }
        if (request.getLogoUrl() != null && request.getLogoUrl().length() > 1000) {
            throw new BusinessException(OwnerErrorCode.STORE_INVALID_LOGO_URL);
        }
        boolean hasOpen = request.getOpenTime() != null;
        boolean hasClose = request.getCloseTime() != null;
        if (hasOpen != hasClose || (hasOpen && request.getOpenTime().equals(request.getCloseTime()))) {
            throw new BusinessException(OwnerErrorCode.STORE_INVALID_BUSINESS_HOURS);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
