package com.main2.delivery.core.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 결제: 지금은 PK·FK 만. 금액·상태·PG 필드는 결제 연동 때 V 파일과 함께 추가 */
@Entity
@Table(name = "payment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private OrderEntity order;

    // 생성은 빌더로만 (PaymentEntity.builder()...build())
    @Builder
    private PaymentEntity(
            OrderEntity order
    ) {
        this.order = order;
    }
}
