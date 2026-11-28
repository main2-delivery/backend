package com.main2.delivery.customer.service.tracking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.main2.delivery.common.exception.BusinessException;
import com.main2.delivery.core.entity.OrderStatus;
import com.main2.delivery.core.event.OrderEvent;
import com.main2.delivery.core.event.OrderEventType;
import com.main2.delivery.customer.dto.tracking.OrderStatusMessage;
import com.main2.delivery.customer.repository.tracking.TrackingRedisRepository;

@ExtendWith(MockitoExtension.class)
class OrderEventHandlerTest {

    @Mock TrackingRedisRepository trackingRedisRepository;
    @Mock TrackingService trackingService;
    @InjectMocks OrderEventHandler orderEventHandler;

    @Test
    @DisplayName("접수 이벤트면 예상 완료 시각(서울 시각)을 저장하고 앱에 보낸다")
    void accepted() {
        OrderEvent event = event("e-1", OrderEventType.ORDER_ACCEPTED, OrderStatus.ACCEPTED, Map.of("cookingMinutes", 15));
        given(trackingRedisRepository.markEventProcessed("e-1")).willReturn(true);

        orderEventHandler.handle(event);

        // 09:20Z = 서울 18:20 → +15분
        LocalDateTime expected = LocalDateTime.of(2026, 11, 28, 18, 35);
        verify(trackingRedisRepository).saveEstimatedReadyAt(1L, expected);

        ArgumentCaptor<OrderStatusMessage> captor = ArgumentCaptor.forClass(OrderStatusMessage.class);
        verify(trackingService).push(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("ACCEPTED");
        assertThat(captor.getValue().getCookingMinutes()).isEqualTo(15);
        assertThat(captor.getValue().getEstimatedReadyAt()).isEqualTo(expected);
    }

    @Test
    @DisplayName("이미 처리한 eventId면 아무것도 하지 않는다")
    void duplicate() {
        OrderEvent event = event("e-1", OrderEventType.ORDER_ACCEPTED, OrderStatus.ACCEPTED, Map.of("cookingMinutes", 15));
        given(trackingRedisRepository.markEventProcessed("e-1")).willReturn(false);

        orderEventHandler.handle(event);

        verify(trackingRedisRepository, never()).saveEstimatedReadyAt(any(), any());
        verify(trackingService, never()).push(any());
    }

    @Test
    @DisplayName("거절 이벤트면 사유와 함께 앱에 보낸다")
    void rejected() {
        OrderEvent event = event("e-2", OrderEventType.ORDER_REJECTED, OrderStatus.REJECTED, Map.of("reason", "재료 소진"));
        given(trackingRedisRepository.markEventProcessed("e-2")).willReturn(true);

        orderEventHandler.handle(event);

        ArgumentCaptor<OrderStatusMessage> captor = ArgumentCaptor.forClass(OrderStatusMessage.class);
        verify(trackingService).push(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("REJECTED");
        assertThat(captor.getValue().getReason()).isEqualTo("재료 소진");
    }

    @Test
    @DisplayName("필수값이 없으면 400, 처리 기록도 남기지 않는다")
    void missingStatus() {
        OrderEvent event = event("e-3", OrderEventType.ORDER_ACCEPTED, null, null);

        assertThatThrownBy(() -> orderEventHandler.handle(event))
                .isInstanceOf(BusinessException.class);
        verify(trackingRedisRepository, never()).markEventProcessed(any());
        verify(trackingService, never()).push(any());
    }

    // 테스트용 이벤트 (2026-11-28 09:20 UTC = 서울 18:20 발생)
    private OrderEvent event(String eventId, OrderEventType type, OrderStatus status, Map<String, Object> payload) {
        return new OrderEvent(eventId, type, Instant.parse("2026-11-28T09:20:00Z"),
                1L, 33167269L, 1L, status, payload);
    }
}