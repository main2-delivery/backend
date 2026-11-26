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
@Table(name = "review")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private StoreEntity store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;        // 수집 리뷰는 null

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", unique = true)
    private OrderEntity order;      // 수집 리뷰는 null. 주문당 리뷰 1개

    @Setter
    @Column(precision = 2, scale = 1)
    private BigDecimal rating;      // 0.5 단위. 별점 없는 리뷰는 null

    @Setter
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(columnDefinition = "TEXT")
    private String body;

    @Setter
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(columnDefinition = "TEXT")
    private String reply;           // 점주 답글

    @Setter
    @Column(name = "replied_at")
    private LocalDateTime repliedAt;

    // 생성은 빌더로만 (ReviewEntity.builder()...build())
    @Builder
    private ReviewEntity(
            StoreEntity store,
            UserEntity user,
            OrderEntity order,
            BigDecimal rating,
            String body
    ) {
        this.store = store;
        this.user = user;
        this.order = order;
        this.rating = rating;
        this.body = body;
    }
}
