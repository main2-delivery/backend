package com.main2.delivery.owner.repository.menu;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.main2.delivery.core.entity.MenuEntity;

public interface MenuRepository extends JpaRepository<MenuEntity, Long>{
    
    // 가게 메뉴 목록
    List<MenuEntity> findByStoreIdAndDeletedFalseOrderByIdAsc(Long storeId);

    // 가게 메뉴
    @EntityGraph(attributePaths = "store")
    Optional<MenuEntity> findByIdAndDeletedFalse(Long id);
}
