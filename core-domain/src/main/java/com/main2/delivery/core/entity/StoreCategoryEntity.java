package com.main2.delivery.core.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "store_category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreCategoryEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    @Column(name = "category_name", nullable = false, length = 50)
    private String categoryName;   // 치킨, 분식 ...

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
}
