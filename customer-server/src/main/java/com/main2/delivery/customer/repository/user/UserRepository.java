package com.main2.delivery.customer.repository.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.main2.delivery.core.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    // 가입 전 중복 확인
    boolean existsByLoginId(String loginId);

    boolean existsByPhone(String phone);

    Optional<UserEntity> findByLoginId(String loginId);
}