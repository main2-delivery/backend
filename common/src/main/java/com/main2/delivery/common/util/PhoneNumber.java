package com.main2.delivery.common.util;

// 휴대전화 번호는 숫자만 저장·비교 (010-1234-5678 → 01012345678)
public final class PhoneNumber {

    private static final String MOBILE_PATTERN = "01\\d{8,9}";

    private PhoneNumber() {
    }

    // 숫자만 남긴 값, 휴대전화 형식이 아니면 null
    public static String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        String digits = raw.replaceAll("[^0-9]", "");
        return digits.matches(MOBILE_PATTERN) ? digits : null;
    }
}
