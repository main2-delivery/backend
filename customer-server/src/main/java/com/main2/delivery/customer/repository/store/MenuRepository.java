package com.main2.delivery.customer.repository.store;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.main2.delivery.core.entity.MenuEntity;
import org.springframework.data.repository.query.Param;

public interface MenuRepository extends JpaRepository<MenuEntity , Long> {

    // 메뉴판: 숨김·삭제 메뉴 제외 , 대표 메뉴가 위
    @Query(value = "select * from menu "
        + "where store_id = :storeId and is_hidden = false and is_deleted = false "
        + "order by is_representative desc, menu_id", nativeQuery = true)
    List<MenuEntity> findMenuBoard(@Param("storeId") Long storeId);
}