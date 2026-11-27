package com.main2.delivery.customer.repository.store;

import org.springframework.data.jpa.repository.JpaRepository;

import com.main2.delivery.core.entity.MenuEntity;

public interface MenuRepository extends JpaRepository<MenuEntity, Long>{
    
}
