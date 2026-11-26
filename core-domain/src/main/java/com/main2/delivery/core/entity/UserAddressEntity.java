package com.main2.delivery.core.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "user_address")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAddressEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(length = 20)
    private String label;              // 집, 회사

    @Column(nullable = false, length = 255)
    private String address;

    @Column(name = "address_detail", length = 100)
    private String addressDetail;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal lat;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal lng;

    @Column(name = "is_default", nullable = false)
    private boolean defaultAddress;
}
