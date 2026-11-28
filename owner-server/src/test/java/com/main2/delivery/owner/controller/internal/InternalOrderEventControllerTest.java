package com.main2.delivery.owner.controller.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.main2.delivery.common.internal.InternalApiKeyFilter;
import com.main2.delivery.common.internal.InternalSecurityConfig;
import com.main2.delivery.common.security.SecurityConfig;
import com.main2.delivery.core.event.OrderEvent;
import com.main2.delivery.core.event.OrderEventType;
import com.main2.delivery.owner.event.OrderEventHandler;

// 공유 키가 맞으면 받은 이벤트를 핸들러에 넘기고, 틀리면 핸들러까지 가지 않는다
@WebMvcTest(controllers = InternalOrderEventController.class,
            properties = "internal.api-key=test-key")
@Import({SecurityConfig.class, InternalSecurityConfig.class})
class InternalOrderEventControllerTest {

    private static final String BODY = """
            {"eventId":"e-1","eventType":"ORDER_CREATED","occurredAt":"2026-11-28T09:00:00Z",
             "orderId":10,"storeId":20,"userId":30,"status":"PENDING","payload":{}}
            """;

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    OrderEventHandler orderEventHandler;

    @Test
    @DisplayName("키가 맞으면 받은 이벤트를 핸들러에 그대로 넘긴다")
    void correctKey_callsHandler() throws Exception {
        mockMvc.perform(post("/internal/order-events")
                        .header(InternalApiKeyFilter.HEADER, "test-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        ArgumentCaptor<OrderEvent> captor = ArgumentCaptor.forClass(OrderEvent.class);
        verify(orderEventHandler).handle(captor.capture());
        assertThat(captor.getValue().eventType()).isEqualTo(OrderEventType.ORDER_CREATED);
        assertThat(captor.getValue().orderId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("키가 없으면 401이고 핸들러를 부르지 않는다")
    void noKey_unauthorized() throws Exception {
        mockMvc.perform(post("/internal/order-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isUnauthorized());

        verify(orderEventHandler, never()).handle(any());
    }
}