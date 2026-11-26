package com.main2.delivery.owner.repository.store;

import com.main2.delivery.core.entity.StoreCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreCategoryRepository extends JpaRepository<StoreCategoryEntity, Long> {
}
