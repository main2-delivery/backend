package com.main2.delivery.customer.controller.order;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.main2.delivery.common.response.ApiResponse;
import com.main2.delivery.customer.dto.order.OrderCancelRequest;
import com.main2.delivery.customer.dto.order.OrderCancelResponse;
import com.main2.delivery.customer.dto.order.OrderCreateRequest;
import com.main2.delivery.customer.dto.order.OrderCreateResponse;
import com.main2.delivery.customer.dto.order.OrderDetailResponse;
import com.main2.delivery.customer.dto.order.OrderListResponse;
import com.main2.delivery.customer.service.order.OrderService;

import lombok.RequiredArgsConstructor;

@RestController 
@RequestMapping("/api/orders")
@RequiredArgsConstructor 
public class OrderController {
    private final OrderService orderService;

    // [1] 주문 생성
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<OrderCreateResponse> createOrder(
            @RequestHeader("X-Test-User-Id") Long userId,
            @RequestBody OrderCreateRequest req) {
        return ApiResponse.ok(orderService.createOrder(userId, req));
    }

    // [2] 내 주문 목록
    @GetMapping
    public ApiResponse<OrderListResponse> getOrders(
            @RequestHeader("X-Test-User-Id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(orderService.getOrders(userId, page, size));
    }

    // [3] 주문 상세
    @GetMapping("/{orderId}")
    public ApiResponse<OrderDetailResponse> getOrder(
            @RequestHeader("X-Test-User-Id") Long userId,
            @PathVariable Long orderId) {
        return ApiResponse.ok(orderService.getOrder(userId, orderId));
    }

    // [4] 접수 전 주문 취소 (사유는 선택)
    @PostMapping("/{orderId}/cancel")
    public ApiResponse<OrderCancelResponse> cancelOrder(
            @RequestHeader("X-Test-User-Id") Long userId,
            @PathVariable Long orderId,
            @RequestBody(required = false) OrderCancelRequest req) {
        return ApiResponse.ok(orderService.cancelOrder(userId, orderId, req));
    }
}
