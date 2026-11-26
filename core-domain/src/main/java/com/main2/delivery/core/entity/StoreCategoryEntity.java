package com.main2.delivery.core.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "store_category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreCategoryEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    @Setter
    @Column(name = "category_name", nullable = false, length = 50)
    private String categoryName;   // 치킨, 분식 ...

    @Setter
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    // 생성은 빌더로만 (StoreCategoryEntity.builder()...build())
    @Builder
    private StoreCategoryEntity(
            String categoryName,
            int sortOrder
    ) {
        this.categoryName = categoryName;
        this.sortOrder = sortOrder;
    }
}
