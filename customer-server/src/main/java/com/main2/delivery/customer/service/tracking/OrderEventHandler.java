package com.main2.delivery.customer.service.tracking;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.main2.delivery.common.exception.BusinessException;
import com.main2.delivery.common.exception.CommonErrorCode;
import com.main2.delivery.core.event.OrderEvent;
import com.main2.delivery.customer.dto.tracking.OrderStatusMessage;
import com.main2.delivery.customer.repository.tracking.TrackingRedisRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 다른 서버에서 온 주문 이벤트 처리 (docs/internal-events.md)
// Sprint 1: InternalOrderEventController가 호출 / Sprint 2(#57): @RabbitListener가 같은 메서드 호출
// HTTP 객체(HttpServletRequest 등)에 의존하지 않음
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventHandler {
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final TrackingRedisRepository trackingRedisRepository;
    private final TrackingService trackingService;

    public void handle(OrderEvent event) {
        checkEvent(event);

        // [1] 같은 eventId는 한 번만 처리 (REST 재시도·MQ 재전송 대비)
        if (!trackingRedisRepository.markEventProcessed(event.eventId())) {
            log.info("이미 처리한 이벤트 eventId={}", event.eventId());
            return;
        }

        switch (event.eventType()) {
            // 고객 서버가 보내는 이벤트라 받을 일이 없음
            case ORDER_CREATED, ORDER_CANCELED ->
                    log.warn("고객 서버가 처리하지 않는 이벤트 type={}", event.eventType());
            // 업주·배달 서버의 상태 변경(접수·거절·조리 완료 …): 상태 값을 그대로 추적 화면에 전달
            // 새 종류가 생겨도 status만 맞으면 그대로 동작
            default -> handleStatusChanged(event);
        }
    }

    private void handleStatusChanged(OrderEvent event) {
        // [2] 접수면 조리 예상 완료 시각 = 발생 시각 + 조리 시간 (서버 간은 Instant, 화면은 서울 시각)
        Integer cookingMinutes = intValue(event.payload(), "cookingMinutes");
        LocalDateTime estimatedReadyAt = trackingRedisRepository.findEstimatedReadyAt(event.orderId());
        if (cookingMinutes != null) {
            LocalDateTime base = event.occurredAt() != null
                    ? LocalDateTime.ofInstant(event.occurredAt(), SEOUL)
                    : LocalDateTime.now(SEOUL);
            estimatedReadyAt = base.plusMinutes(cookingMinutes);
            trackingRedisRepository.saveEstimatedReadyAt(event.orderId(), estimatedReadyAt);
        }

        // [3] 구독 중인 앱 화면에 전달
        trackingService.push(new OrderStatusMessage(
                event.orderId(), event.status().name(), cookingMinutes,
                estimatedReadyAt, stringValue(event.payload(), "reason")));
    }

    // 필수값 확인
    private void checkEvent(OrderEvent event) {
        if (event.eventId() == null || event.eventId().isBlank() || event.eventType() == null
                || event.orderId() == null || event.status() == null) {
            throw new BusinessException(CommonErrorCode.COMMON_INVALID_INPUT, "eventId, eventType, orderId, status는 필수");
        }
    }

    // payload에서 숫자 꺼내기 (JSON 숫자는 Integer/Long 등으로 들어옴)
    private Integer intValue(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        if (value == null) {
            return null;
        }
        return (value instanceof Number number) ? number.intValue() : Integer.valueOf(value.toString());
    }

    private String stringValue(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        return value == null ? null : value.toString();
    }
}