package com.main2.delivery.customer.controller.order;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.main2.delivery.common.response.ApiResponse;
import com.main2.delivery.customer.dto.order.OrderCreateRequest;
import com.main2.delivery.customer.dto.order.OrderCreateResponse;
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

}
