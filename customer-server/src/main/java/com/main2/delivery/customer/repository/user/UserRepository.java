package com.main2.delivery.customer.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;

import com.main2.delivery.core.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    
}