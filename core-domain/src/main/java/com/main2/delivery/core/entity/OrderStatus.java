package com.main2.delivery.core.entity;

public enum OrderStatus {
    PENDING,     // 접수 대기
    ACCEPTED,    // 접수
    COOKING,     // 조리 중
    COOKED,      // 조리 완료
    DELIVERING,  // 배달 중
    DELIVERED,   // 배달 완료
    REJECTED,    // 점주 거절
    CANCELED     // 고객 취소
}