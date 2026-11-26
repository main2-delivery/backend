package com.main2.delivery.core.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Setter
    @Column(length = 20)
    private String label;              // 집, 회사

    @Setter
    @Column(nullable = false, length = 255)
    private String address;

    @Setter
    @Column(name = "address_detail", length = 100)
    private String addressDetail;

    @Setter
    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal lat;

    @Setter
    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal lng;

    @Setter
    @Column(name = "is_default", nullable = false)
    private boolean defaultAddress;

    // 생성은 빌더로만 (UserAddressEntity.builder()...build())
    @Builder
    private UserAddressEntity(
            UserEntity user,
            String label,
            String address,
            String addressDetail,
            BigDecimal lat,
            BigDecimal lng,
            boolean defaultAddress
    ) {
        this.user = user;
        this.label = label;
        this.address = address;
        this.addressDetail = addressDetail;
        this.lat = lat;
        this.lng = lng;
        this.defaultAddress = defaultAddress;
    }
}
