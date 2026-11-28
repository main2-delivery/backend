package com.main2.delivery.owner.dto.store;

import java.util.Arrays;
import java.util.Optional;

// 가게 영업 상태. DB store.status 컬럼(VARCHAR)에 이 이름 그대로 저장된다.
// 고객 서버(가게 목록 정렬·주문 가능 판단)도 같은 문자열을 쓰므로 값을 바꾸면 고객A·고객B와 맞춰야 한다.
public enum StoreStatus {
    OPEN,       // 영업 중 (주문 가능)
    PREPARING,  // 준비 중 (입점 직후 기본값)
    PAUSED,     // 일시 중지
    CLOSED;     // 영업 종료

    public static Optional<StoreStatus> from(String value) {
        if (value == null) {
            return Optional.empty();
        }
        String normalized = value.trim().toUpperCase();
        return Arrays.stream(values()).filter(s -> s.name().equals(normalized)).findFirst();
    }
}
