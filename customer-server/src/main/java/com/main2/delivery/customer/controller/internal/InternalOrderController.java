package com.main2.delivery.customer.controller.internal;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.main2.delivery.common.response.ApiResponse;
import com.main2.delivery.customer.dto.internal.OrderEvent;
import com.main2.delivery.customer.service.tracking.OrderEventHandler;

import lombok.RequiredArgsConstructor;

// 서버 간 호출 입구 (X-Internal-Api-Key 필요)
// Sprint 2(DUDU-57): @RabbitListener가 같은 핸들러를 호출하고 이 컨트롤러는 삭제
@RestController
@RequestMapping("/internal/orders")
@RequiredArgsConstructor
public class InternalOrderController {
    private final OrderEventHandler orderEventHandler;

    // [1] 업주 서버 → 주문 상태 변경 알림
    @PostMapping("/status-changed")
    public ApiResponse<Void> statusChanged(@RequestBody OrderEvent event) {
        orderEventHandler.handleStatusChanged(event);
        return ApiResponse.ok();
    }
}