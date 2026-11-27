package com.main2.delivery.core.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
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
    @Setter
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

    @Column(name = "order_key", nullable = false, length = 64, unique = true)
    private String orderKey;        // 중복 주문 방지 키 (앱이 주문서 진입 시 생성)

    @Column(name = "request_note", length = 255)
    private String requestNote;

    @Column(name = "delivery_address_detail", length = 100)
    private String deliveryAddressDetail;   // 주문 시점 복사

    @Column(name = "delivery_lat", nullable = false, precision = 10, scale = 7)
    private BigDecimal deliveryLat;         // 주문 시점 복사

    @Column(name = "delivery_lng", nullable = false, precision = 10, scale = 7)
    private BigDecimal deliveryLng;         // 주문 시점 복사

    @Column(name = "payment_method", nullable = false, length = 20)
    private String paymentMethod;   // CARD / EASY_PAY (Sprint 1은 저장만)

    @Version
    private Long version;           // 낙관적 락 (접수 vs 취소 동시 요청)

    @Setter
    @Column(name = "cancel_reason", length = 255)
    private String cancelReason;    // 거절·취소 사유

    @Setter
    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Setter
    @Column(name = "cooked_at")
    private LocalDateTime cookedAt;

    @Setter
    @Column(name = "completed_at")
    private LocalDateTime completedAt;   // 배달 완료

    @Setter
    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    // 생성은 빌더로만 (OrderEntity.builder()...build()). status 는 PENDING 으로 시작. 금액(menuAmount·deliveryFee·totalPrice)은 주문 후 변경 불가라 Setter 없음
    @Builder
    private OrderEntity(
            String orderKey,
            UserEntity user,
            StoreEntity store,
            int menuAmount,
            int deliveryFee,
            String deliveryAddress,
            String deliveryAddressDetail,
            BigDecimal deliveryLat,
            BigDecimal deliveryLng,
            String requestNote,
            String paymentMethod
    ) {
        this.orderKey = orderKey;
        this.user = user;
        this.store = store;
        this.menuAmount = menuAmount;
        this.deliveryFee = deliveryFee;
        this.deliveryAddress = deliveryAddress;
        this.deliveryAddressDetail = deliveryAddressDetail;
        this.deliveryLat = deliveryLat;
        this.deliveryLng = deliveryLng;
        this.requestNote = requestNote;
        this.paymentMethod = paymentMethod;
        this.totalPrice = menuAmount + deliveryFee;   // 합계는 서버에서 계산
    }
}
