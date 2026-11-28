package com.main2.delivery.customer.controller.tracking;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.main2.delivery.common.response.ApiResponse;
import com.main2.delivery.common.security.LoginUser;
import com.main2.delivery.customer.dto.tracking.TrackingResponse;
import com.main2.delivery.customer.service.tracking.TrackingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/customer/orders/{orderId}")
@RequiredArgsConstructor
public class TrackingController {
    private final TrackingService trackingService;

    // [1] 현재 추적 정보
    @GetMapping("/tracking")
    public ApiResponse<TrackingResponse> getTracking(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long orderId) {
        return ApiResponse.ok(trackingService.getTracking(loginUser.id(), orderId));
    }

    // [2] 실시간 상태 구독 (SSE). ApiResponse로 감싸지 않고 이벤트 스트림 그대로
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long orderId) {
        return trackingService.subscribe(loginUser.id(), orderId);
    }
}