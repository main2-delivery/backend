package com.main2.delivery.core.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;

/** 업주 회원 (업주 웹) */
@Entity
@DynamicInsert
@Table(name = "owner")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OwnerEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "owner_id")
    private Long id;

    @Column(name = "login_id", nullable = false, unique = true, length = 50)
    private String loginId;        // 로그인 ID (가입 후 변경 불가라 @Setter 없음)

    @Column(nullable = false, length = 20)
    private String phone;          // 중복 가입 방지용. 숫자만

    @Setter
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Setter
    @Column(nullable = false, length = 50)
    private String name;

    @Setter
    @Column(nullable = false, length = 20)
    private String status;         // ACTIVE / WITHDRAWN (기본값 ACTIVE 는 DB 가 넣음)

    // 생성은 빌더로만 (OwnerEntity.builder()...build()). status 는 넣지 않음 → DB 기본값 ACTIVE
    @Builder
    private OwnerEntity(
            String loginId,
            String phone,
            String passwordHash,
            String name
    ) {
        this.loginId = loginId;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.name = name;
    }
}
