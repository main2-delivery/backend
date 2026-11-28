package com.main2.delivery.core.event;

// 주문 이벤트 종류. MQ로 바뀌면 이 이름이 라우팅 키가 됨 (#57)
public enum OrderEventType {
    ORDER_CREATED,    // 고객 → 업주: 새 주문
    ORDER_CANCELED,   // 고객 → 업주: 접수 전 취소
    ORDER_ACCEPTED,   // 업주 → 고객: 접수
    ORDER_REJECTED,   // 업주 → 고객: 거절
    ORDER_COOKED      // 업주 → 배달: 조리 완료 (배차 시작)
}