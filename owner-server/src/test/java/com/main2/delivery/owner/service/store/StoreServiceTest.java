package com.main2.delivery.owner.service.store;

import com.main2.delivery.core.entity.OwnerEntity;
import com.main2.delivery.core.entity.StoreEntity;
import com.main2.delivery.owner.dto.store.StoreCreateRequestDto;
import com.main2.delivery.owner.dto.store.StoreCreateResponseDto;
import com.main2.delivery.owner.dto.store.StoreDetailResponseDto;
import com.main2.delivery.owner.dto.store.StoreUpdateRequestDto;
import com.main2.delivery.common.exception.ErrorCode;
import com.main2.delivery.owner.exception.OwnerErrorCode;
import com.main2.delivery.common.exception.BusinessException;
import com.main2.delivery.owner.repository.owner.OwnerRepository;
import com.main2.delivery.owner.repository.store.StoreCategoryRepository;
import com.main2.delivery.owner.repository.store.StoreRepository;
import com.main2.delivery.common.geocode.GeoCodingService;
import com.main2.delivery.common.geocode.GeoPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

// 가게 서비스 규칙 확인 (DB 없이 가짜 리포지토리로 실행)
@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    private static final Long OWNER_ID = 1L;
    private static final Long OTHER_OWNER_ID = 2L;
    private static final GeoPoint POINT = new GeoPoint(new BigDecimal("37.3943000"), new BigDecimal("126.9568000"));

    @Mock StoreRepository storeRepository;
    @Mock StoreCategoryRepository storeCategoryRepository;
    @Mock OwnerRepository ownerRepository;
    @Mock GeoCodingService geoCodingService;
    @InjectMocks StoreService storeService;

    private StoreCreateRequestDto createRequest() {
        return StoreCreateRequestDto.builder()
                .name("띱치킨 안양점")
                .address("경기도 안양시 동안구 시민대로 100")
                .phone("031-123-4567")
                .businessNumber("123-45-67890")
                .build();
    }

    private StoreUpdateRequestDto updateRequest() {
        return StoreUpdateRequestDto.builder()
                .phone("031-765-4321")
                .openTime(LocalTime.of(11, 0))
                .closeTime(LocalTime.of(23, 0))
                .minOrderAmount(15000)
                .deliveryFee(3000)
                .deliveryRadiusM(2000)
                .build();
    }

    private StoreEntity storeOwnedBy(Long ownerId) {
        OwnerEntity storeOwner = mock(OwnerEntity.class);
        given(storeOwner.getId()).willReturn(ownerId);
        StoreEntity store = StoreEntity.builder()
                .owner(storeOwner)
                .storeName("띱치킨")
                .storePhone("031-123-4567")
                .businessNumber("123-45-67890")
                .address("경기도 안양시")
                .lat(POINT.getLat())
                .lng(POINT.getLng())
                .build();
        ReflectionTestUtils.setField(store, "id", 10L);
        return store;
    }

    private void assertError(Runnable action, ErrorCode expected) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(BusinessException.class, e -> assertThat(e.getErrorCode()).isEqualTo(expected));
    }

    @Test
    @DisplayName("입점 신청하면 주소 좌표가 채워지고 영업 상태는 준비 중으로 만들어진다")
    void apply_createsStoreWithCoordinates() {
        given(ownerRepository.findById(OWNER_ID)).willReturn(Optional.of(mock(OwnerEntity.class)));
        given(geoCodingService.geocode(anyString())).willReturn(Optional.of(POINT));
        given(storeRepository.save(any(StoreEntity.class))).willAnswer(inv -> {
            StoreEntity saved = inv.getArgument(0);
            assertThat(saved.getStatus()).isEqualTo("PREPARING");
            assertThat(saved.getDeliveryRadiusM()).isEqualTo(3000);
            ReflectionTestUtils.setField(saved, "id", 99L);
            return saved;
        });

        StoreCreateResponseDto response = storeService.apply(OWNER_ID, createRequest());

        assertThat(response.getStoreId()).isEqualTo(99L);
        assertThat(response.getLat()).isEqualByComparingTo(POINT.getLat());
        assertThat(response.getLng()).isEqualByComparingTo(POINT.getLng());
    }

    @Test
    @DisplayName("주소를 좌표로 바꾸지 못하면 가게를 만들지 않는다")
    void apply_failsWhenGeocodingFails() {
        given(ownerRepository.findById(OWNER_ID)).willReturn(Optional.of(mock(OwnerEntity.class)));
        given(geoCodingService.geocode(anyString())).willReturn(Optional.empty());

        assertError(() -> storeService.apply(OWNER_ID, createRequest()), OwnerErrorCode.STORE_GEOCODING_FAILED);
        verify(storeRepository, never()).save(any());
    }

    @Test
    @DisplayName("사업자등록번호·전화번호 형식이 틀리면 400")
    void apply_invalidFormats() {
        StoreCreateRequestDto badBusinessNumber = createRequest();
        badBusinessNumber.setBusinessNumber("1234567890");
        StoreCreateRequestDto badPhone = createRequest();
        badPhone.setPhone("0311234567");

        assertError(() -> storeService.apply(OWNER_ID, badBusinessNumber), OwnerErrorCode.STORE_INVALID_BUSINESS_NUMBER);
        assertError(() -> storeService.apply(OWNER_ID, badPhone), OwnerErrorCode.STORE_INVALID_PHONE);
        verify(storeRepository, never()).save(any());
    }

    @Test
    @DisplayName("없는 카테고리로 신청하면 400")
    void apply_failsWhenCategoryMissing() {
        given(ownerRepository.findById(OWNER_ID)).willReturn(Optional.of(mock(OwnerEntity.class)));
        given(storeCategoryRepository.findById(7L)).willReturn(Optional.empty());
        StoreCreateRequestDto request = createRequest();
        request.setCategoryId(7L);

        assertError(() -> storeService.apply(OWNER_ID, request), OwnerErrorCode.STORE_CATEGORY_NOT_FOUND);
    }

    @Test
    @DisplayName("다른 업주의 가게를 조회·수정하면 403")
    void otherOwnersStore_forbidden() {
        StoreEntity otherStore = storeOwnedBy(OTHER_OWNER_ID);
        given(storeRepository.findById(10L)).willReturn(Optional.of(otherStore));

        assertError(() -> storeService.getMyStore(OWNER_ID, 10L), OwnerErrorCode.STORE_NOT_OWNER);
        assertError(() -> storeService.updateMyStore(OWNER_ID, 10L, updateRequest()), OwnerErrorCode.STORE_NOT_OWNER);
    }

    @Test
    @DisplayName("없는 가게를 조회하면 404")
    void getMyStore_notFound() {
        given(storeRepository.findById(10L)).willReturn(Optional.empty());

        assertError(() -> storeService.getMyStore(OWNER_ID, 10L), OwnerErrorCode.STORE_NOT_FOUND);
    }

    @Test
    @DisplayName("내 가게 정보를 수정하면 최소주문금액·배달팁·반경·영업시간이 바뀐다")
    void updateMyStore_updatesInfo() {
        StoreEntity store = storeOwnedBy(OWNER_ID);
        given(storeRepository.findById(10L)).willReturn(Optional.of(store));

        StoreDetailResponseDto response = storeService.updateMyStore(OWNER_ID, 10L, updateRequest());

        assertThat(response.getMinOrderAmount()).isEqualTo(15000);
        assertThat(response.getDeliveryFee()).isEqualTo(3000);
        assertThat(response.getDeliveryRadiusM()).isEqualTo(2000);
        assertThat(response.getOpenTime()).isEqualTo(LocalTime.of(11, 0));
        assertThat(response.getPhone()).isEqualTo("031-765-4321");
    }

    @Test
    @DisplayName("금액이 음수, 반경이 0, 영업시간이 하나만 있으면 400")
    void updateMyStore_invalidValues() {
        StoreUpdateRequestDto negativeFee = updateRequest();
        negativeFee.setDeliveryFee(-1);
        StoreUpdateRequestDto zeroRadius = updateRequest();
        zeroRadius.setDeliveryRadiusM(0);
        StoreUpdateRequestDto onlyOpen = updateRequest();
        onlyOpen.setCloseTime(null);

        assertError(() -> storeService.updateMyStore(OWNER_ID, 10L, negativeFee), OwnerErrorCode.STORE_INVALID_AMOUNT);
        assertError(() -> storeService.updateMyStore(OWNER_ID, 10L, zeroRadius), OwnerErrorCode.STORE_INVALID_DELIVERY_RADIUS);
        assertError(() -> storeService.updateMyStore(OWNER_ID, 10L, onlyOpen), OwnerErrorCode.STORE_INVALID_BUSINESS_HOURS);
    }
}
