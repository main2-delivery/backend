package com.main2.delivery.customer.repository.store;

import org.springframework.data.jpa.repository.JpaRepository;

import com.main2.delivery.core.entity.StoreEntity;

public interface StoreRepository extends JpaRepository<StoreEntity, Long>{
    
}
