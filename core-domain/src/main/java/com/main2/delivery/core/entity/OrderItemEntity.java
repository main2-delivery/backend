package com.main2.delivery.core.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItemEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id")
    private MenuEntity menu;        // 메뉴가 지워지면 null

    @Column(name = "menu_name", nullable = false, length = 100)
    private String menuName;        // 주문 시점 값

    @Column(name = "unit_price", nullable = false)
    private int unitPrice;          // 주문 시점 값

    @Column(nullable = false)
    private int quantity;
}
