package com.main2.delivery.core.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "menu")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private StoreEntity store;

    @Column(name = "menu_name", nullable = false, length = 100)
    private String menuName;

    @Column(length = 500)
    private String description;

    private Integer price;          // null 이면 가격 미표기 (주문 불가)

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "is_sold_out", nullable = false)
    private boolean soldOut;

    @Column(name = "is_hidden", nullable = false)
    private boolean hidden;

    @Column(name = "is_representative", nullable = false)
    private boolean representative;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;        // 삭제 표시 (주문 이력 보존)
}
