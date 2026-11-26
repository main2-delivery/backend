package com.main2.delivery.customer.service.address;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.main2.delivery.common.exception.BusinessException;
import com.main2.delivery.common.exception.CommonErrorCode;
import com.main2.delivery.core.entity.UserAddressEntity;
import com.main2.delivery.core.entity.UserEntity;
import com.main2.delivery.customer.dto.address.UserAddressRequest;
import com.main2.delivery.customer.dto.address.UserAddressResponse;
import com.main2.delivery.customer.repository.address.UserAddressRepository;
import com.main2.delivery.customer.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserAddressService {

    private final UserAddressRepository addressRepository;
    private final UserRepository userRepository;

    // [1] 내 주소 목록
    public List<UserAddressResponse> findMyAddresses(Long userId) {
        List<UserAddressEntity> entities =
                addressRepository.findByUserIdOrderByDefaultAddressDescIdDesc(userId);
        List<UserAddressResponse> result = new ArrayList<>();
        for (UserAddressEntity entity : entities) {
            result.add(new UserAddressResponse(entity));
        }
        return result;
    }

    // [2] 주소 등록 (내 첫 주소면 자동으로 기본 주소)
    @Transactional
    public UserAddressResponse create(Long userId, UserAddressRequest req) {
        checkRequest(req);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.COMMON_NOT_FOUND, "사용자 없음"));
        boolean first = addressRepository.countByUserId(userId) == 0;
        UserAddressEntity entity = UserAddressEntity.builder()
                .user(user)
                .label(req.getLabel())
                .address(req.getAddress())
                .addressDetail(req.getAddressDetail())
                .lat(req.getLat())
                .lng(req.getLng())
                .defaultAddress(first)
                .build();
        addressRepository.save(entity);
        return new UserAddressResponse(entity);
    }

    // [3] 주소 수정 (내 주소만)
    @Transactional
    public UserAddressResponse update(Long userId, Long addressId, UserAddressRequest req) {
        checkRequest(req);
        UserAddressEntity entity = findMine(userId, addressId);
        entity.setLabel(req.getLabel());
        entity.setAddress(req.getAddress());
        entity.setAddressDetail(req.getAddressDetail());
        entity.setLat(req.getLat());
        entity.setLng(req.getLng());
        return new UserAddressResponse(entity);
    }

    // [4] 주소 삭제 (기본 주소를 지우면 남은 것 중 최근 주소가 기본이 됨)
    @Transactional
    public void delete(Long userId, Long addressId) {
        UserAddressEntity entity = findMine(userId, addressId);
        boolean wasDefault = entity.isDefaultAddress();
        addressRepository.delete(entity);
        if (wasDefault) {
            UserAddressEntity next = addressRepository.findFirstByUserIdOrderByIdDesc(userId)
                    .orElse(null);
            if (next != null) {
                next.setDefaultAddress(true);
            }
        }
    }

    // [5] 기본 주소 변경 (기본은 항상 하나)
    @Transactional
    public UserAddressResponse changeDefault(Long userId, Long addressId) {
        UserAddressEntity target = findMine(userId, addressId);
        UserAddressEntity old = addressRepository.findByUserIdAndDefaultAddressTrue(userId)
                .orElse(null);
        if (old != null) {
            old.setDefaultAddress(false);
        }
        target.setDefaultAddress(true);
        return new UserAddressResponse(target);
    }

    // 내 주소 한 건 찾기 (없거나 남의 주소면 404)
    private UserAddressEntity findMine(Long userId, Long addressId) {
        return addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.COMMON_NOT_FOUND, "주소 없음"));
    }

    // 필수값 확인 (없으면 400)
    private void checkRequest(UserAddressRequest req) {
        if (req.getAddress() == null || req.getAddress().isBlank()
                || req.getLat() == null || req.getLng() == null) {
            throw new BusinessException(CommonErrorCode.COMMON_INVALID_INPUT, "주소와 좌표는 필수");
        }
    }
}