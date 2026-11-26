package com.main2.delivery.core.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

    @Column(nullable = false, length = 20)
    private String phone;          // 로그인 ID. 숫자만

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 20)
    private String status;         // ACTIVE / WITHDRAWN (기본값 ACTIVE 는 DB 가 넣음)
}
