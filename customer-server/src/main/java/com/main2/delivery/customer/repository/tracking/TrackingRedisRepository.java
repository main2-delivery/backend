package com.main2.delivery.customer.repository.tracking;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

// 주문추적용 Redis
// order:eta:{orderId}         → 조리 예상 완료 시각 (DB 컬럼이 없어서 여기 보관)
// event:processed:{eventId}   → 이미 처리한 서버 간 알림 (중복 처리 방지)
@Repository 
@RequiredArgsConstructor 
public class TrackingRedisRepository {
    private static final Duration TTL = Duration.ofDays(1);

    private final StringRedisTemplate redis;

    // 조리 예상 완료 시각 저장
    public void saveEstimatedReadyAt(Long orderId, LocalDateTime estimatedReadyAt){
        redis.opsForValue().set(etaKey(orderId), estimatedReadyAt.toString(), TTL);
    }

    // 조리 에상 완료 시각 (없으면 null)
    public LocalDateTime findEstimatedReadyAt(Long orderId){
        String value = redis.opsForValue().get(etaKey(orderId));
        return value == null ? null : LocalDateTime.parse(value);
    }

    // 처음 받은 eventId면 true, 이미 처리한 거면 false (SETNX: 없을때만 저장)
    public boolean markEventProcessed(String eventId){
        Boolean first = redis.opsForValue().setIfAbsent("event:processed" + eventId, "1", TTL);
        return Boolean.TRUE.equals(first);
    }

    private String etaKey(Long orderId){
        return "order:eta" + orderId;
    }
}
