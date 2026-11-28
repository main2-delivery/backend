package com.main2.delivery.customer.repository.store;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.main2.delivery.core.entity.StoreCategoryEntity;

public interface StoreCategoryRepository extends JpaRepository<StoreCategoryEntity , Long>{
    
    // 카테고리 전체를 화면 표시 순서대로
    List<StoreCategoryEntity> findAllByOrderBySortOrderAscIdAsc();
    
}
