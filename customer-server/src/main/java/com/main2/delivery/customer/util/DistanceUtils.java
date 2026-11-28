package com.main2.delivery.customer.util;

import java.math.BigDecimal;

// 위도·경도 두 점 사이 거리 (하버사인 공식, 미터)
// 가게 목록(StoreService)과 주문 배달 반경 검사(OrderCreator)가 같은 계산을 쓰도록 한 곳에 둠
public class DistanceUtils {

    private static final double EARTH_RADIUS_M = 6_371_000;

    private DistanceUtils() {
    }

    public static double meters(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return EARTH_RADIUS_M * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    // DB 좌표(BigDecimal)용. 계산은 위 메서드로
    public static double meters(BigDecimal lat1, BigDecimal lng1, BigDecimal lat2, BigDecimal lng2) {
        return meters(lat1.doubleValue(), lng1.doubleValue(), lat2.doubleValue(), lng2.doubleValue());
    }
}