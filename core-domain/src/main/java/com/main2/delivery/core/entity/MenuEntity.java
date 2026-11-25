package com.main2.delivery.core.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "menu")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private StoreEntity store;

    @Column(name = "menu_name", nullable = false, length = 100)
    private String menuName;

    private Integer price;     // null이면 가격 미표기 (주문 불가)

    @Column(name = "is_sold_out", nullable = false)
    private boolean soldOut;

    @Column(name = "menu_category", length = 50)
    private String menuCategory;
}