package com.main2.delivery.customer.service.tracking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

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
import com.main2.delivery.customer.dto.internal.OrderEvent;
import com.main2.delivery.customer.dto.tracking.OrderStatusMessage;
import com.main2.delivery.customer.repository.tracking.TrackingRedisRepository;

@ExtendWith(MockitoExtension.class)
class OrderEventHandlerTest {

    @Mock TrackingRedisRepository trackingRedisRepository;
    @Mock TrackingService trackingService;
    @InjectMocks OrderEventHandler orderEventHandler;

    @Test
    @DisplayName("접수 알림이면 예상 완료 시각을 저장하고 앱에 보낸다")
    void accepted() {
        OrderEvent event = event("e-1", "ACCEPTED", Map.of("cookingMinutes", 15));
        given(trackingRedisRepository.markEventProcessed("e-1")).willReturn(true);

        orderEventHandler.handleStatusChanged(event);

        LocalDateTime expected = LocalDateTime.of(2026, 11, 28, 18, 35);
        verify(trackingRedisRepository).saveEstimatedReadyAt(1L, expected);

        // push에 넘어간 메시지를 붙잡아서 내용 확인
        ArgumentCaptor<OrderStatusMessage> captor = ArgumentCaptor.forClass(OrderStatusMessage.class);
        verify(trackingService).push(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("ACCEPTED");
        assertThat(captor.getValue().getCookingMinutes()).isEqualTo(15);
        assertThat(captor.getValue().getEstimatedReadyAt()).isEqualTo(expected);
    }

    @Test
    @DisplayName("이미 처리한 eventId면 아무것도 하지 않는다")
    void duplicate() {
        OrderEvent event = event("e-1", "ACCEPTED", Map.of("cookingMinutes", 15));
        given(trackingRedisRepository.markEventProcessed("e-1")).willReturn(false);

        orderEventHandler.handleStatusChanged(event);

        verify(trackingRedisRepository, never()).saveEstimatedReadyAt(any(), any());
        verify(trackingService, never()).push(any());
    }

    @Test
    @DisplayName("알 수 없는 상태 값이면 400, 처리 기록도 남기지 않는다")
    void unknownStatus() {
        OrderEvent event = event("e-1", "PAID", null);

        assertThatThrownBy(() -> orderEventHandler.handleStatusChanged(event))
                .isInstanceOf(BusinessException.class);
        verify(trackingRedisRepository, never()).markEventProcessed(any());
        verify(trackingService, never()).push(any());
    }

    // 테스트용 이벤트 (18:20에 발생)
    private OrderEvent event(String eventId, String status, Map<String, Object> payload) {
        OrderEvent event = new OrderEvent();
        event.setEventId(eventId);
        event.setEventType("ORDER_STATUS_CHANGED");
        event.setOccurredAt(LocalDateTime.of(2026, 11, 28, 18, 20));
        event.setOrderId(1L);
        event.setStoreId(33167269L);
        event.setUserId(1L);
        event.setStatus(status);
        event.setPayload(payload);
        return event;
    }
}