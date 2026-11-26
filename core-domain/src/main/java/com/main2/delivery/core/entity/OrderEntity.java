package com.main2.delivery.core.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderEntity extends BaseTimeEntity {   // created_at = 주문 시각

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private StoreEntity store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    // 상태 전이가 있어서 enum 사용. DB 는 VARCHAR 라 VARCHAR 로 매핑
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "menu_amount", nullable = false)
    private int menuAmount;         // 단가 × 수량 합계

    @Column(name = "delivery_fee", nullable = false)
    private int deliveryFee;

    @Column(name = "total_price", nullable = false)
    private int totalPrice;         // 메뉴 합계 + 배달비

    @Column(name = "delivery_address", nullable = false, length = 255)
    private String deliveryAddress; // 주문 시점 복사

    @Column(name = "request_note", length = 255)
    private String requestNote;

    @Column(name = "cancel_reason", length = 255)
    private String cancelReason;    // 거절·취소 사유

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "cooked_at")
    private LocalDateTime cookedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;   // 배달 완료

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;
}
