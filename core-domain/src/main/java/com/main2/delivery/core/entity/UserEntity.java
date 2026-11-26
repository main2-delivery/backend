package com.main2.delivery.core.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

/** 개인 회원 (고객 앱) */
@Entity
@DynamicInsert   // null 인 필드는 INSERT 에서 빼서 DB 기본값이 들어가게 함 (status)
@Table(name = "`user`")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, length = 20)
    private String phone;          // 로그인 ID. 숫자만

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 50)
    private String nickname;       // 리뷰 표시명

    @Column(nullable = false, length = 20)
    private String status;         // ACTIVE / WITHDRAWN (기본값 ACTIVE 는 DB 가 넣음)
}
