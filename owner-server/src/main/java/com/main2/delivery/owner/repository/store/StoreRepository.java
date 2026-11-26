package com.main2.delivery.owner.repository.store;

import com.main2.delivery.core.entity.StoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreRepository extends JpaRepository<StoreEntity, Long> {

    List<StoreEntity> findAllByOwnerIdOrderByIdAsc(Long ownerId);
}
