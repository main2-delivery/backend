package com.main2.delivery.customer.repository.address;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.main2.delivery.core.entity.UserAddressEntity;

public interface UserAddressRepository extends JpaRepository<UserAddressEntity, Long> {

    // 내 주소 목록 (기본 주소가 위, 그다음 최근 등록순)
    List<UserAddressEntity> findByUserIdOrderByDefaultAddressDescIdDesc(Long userId);

    // 내 주소 한 건. 남의 주소 번호면 비어서 나옴 → 404 처리용
    Optional<UserAddressEntity> findByIdAndUserId(Long id, Long userId);

    // 현재 기본 주소
    Optional<UserAddressEntity> findByUserIdAndDefaultAddressTrue(Long userId);

    // 내 주소 개수 (0이면 첫 주소)
    long countByUserId(Long userId);

    // 가장 최근에 등록한 내 주소 한 건 (기본 주소 삭제 후 다음 기본 후보)
    Optional<UserAddressEntity> findFirstByUserIdOrderByIdDesc(Long userId);
}