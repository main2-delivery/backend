package com.main2.delivery.customer.service.tracking;

import java.time.LocalDateTime;
import java.util.Map;

import javax.sound.midi.Track;

import org.springframework.stereotype.Component;

import com.main2.delivery.common.exception.BusinessException;
import com.main2.delivery.common.exception.CommonErrorCode;
import com.main2.delivery.core.entity.OrderStatus;
import com.main2.delivery.customer.dto.internal.OrderEvent;
import com.main2.delivery.customer.dto.tracking.OrderStatusMessage;
import com.main2.delivery.customer.repository.tracking.TrackingRedisRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 업주 서버 이벤트 처리 (DUDU-8 규칙)
// Sprint 1: InternalOrderController가 호출 / Sprint 2: @RabbitListener가 같은 메서드 호출
// HTTP 객체(HttpServletRequest 등)에 의존하지 않음
@Slf4j 
@Component 
@RequiredArgsConstructor 
public class OrderEventHandler {
    private final TrackingRedisRepository trackingRedisRepository;
    private final TrackingService trackingService;

    public void handleStatusChanged(OrderEvent event){
        checkEvent(event);

        // [1] 같은 eventId는 한번만 처리 (REST 재시도 / MQ 재전송 대비)
        if(!trackingRedisRepository.markEventProcessed(event.getEventId())){
            log.info("이미 처리한 이벤트 eventId={}", event.getEventId());
            return;
        }

        // [2] 접수 알림이면 조리 예상 완료 시각 = 발생 시각 + 조리 시간
        Integer cookingMinutes = intValue(event.getPayload(), "cookingMinutes");
        LocalDateTime estimatedReadyAt = trackingRedisRepository.findEstimatedReadyAt(event.getOrderId());
        if(cookingMinutes != null){
            LocalDateTime base = event.getOccurredAt() != null ? event.getOccurredAt() : LocalDateTime.now();
            estimatedReadyAt = base.plusMinutes(cookingMinutes);
            trackingRedisRepository.saveEstimatedReadyAt(event.getOrderId(), estimatedReadyAt);
        }

        // [3] 구독 중인 앱 화면 전달
        trackingService.push(new OrderStatusMessage(event.getOrderId(), event.getStatus(), cookingMinutes, estimatedReadyAt, stringValue(event.getPayload(), "reason")));
    }

    // 필수값 상태 값 확인
    private void checkEvent(OrderEvent event){
        if(event.getEventId() == null || event.getEventId().isBlank() || event.getOrderId() == null || event.getStatus() == null){
            throw new BusinessException(CommonErrorCode.COMMON_INVALID_INPUT, "eventId, orderId, status는 필수");
        }
        try{
            OrderStatus.valueOf(event.getStatus());
        }catch(IllegalArgumentException e){
            throw new BusinessException(CommonErrorCode.COMMON_INVALID_INPUT, "알 수 없는 주문 상태: " + event.getStatus());
        }
    }

    // payload에서 숫자 꺼내기 (JSON 숫자는 Integer/Long 등으로 들어옴)
    private Integer intValue(Map<String, Object> payload, String key){
        if(payload == null || payload.get(key) == null){
            return null;
        }
        Object value = payload.get(key);
        return (value instanceof Number number) ? number.intValue() : Integer.valueOf(value.toString());
    }

    // payload에서 문자열 꺼내기
    private String stringValue(Map<String, Object> payload, String key){
        if(payload == null || payload.get(key) == null){
            return null;
        }
        return payload.get(key).toString();
    }
}
