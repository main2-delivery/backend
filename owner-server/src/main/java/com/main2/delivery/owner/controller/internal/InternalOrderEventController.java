package com.main2.delivery.owner.controller.internal;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.main2.delivery.common.response.ApiResponse;
import com.main2.delivery.core.event.OrderEvent;
import com.main2.delivery.owner.event.OrderEventHandler;

import lombok.RequiredArgsConstructor;

// 고객 서버 → 업주 서버 주문 이벤트 (공유 키는 InternalApiKeyFilter가 검사)
// 컨트롤러는 받기만 하고 처리는 핸들러에 (MQ로 바뀌면 이 클래스만 없어짐)
@RestController 
@RequestMapping("/internal/order-events")
@RequiredArgsConstructor 
public class InternalOrderEventController {

    private final OrderEventHandler orderEventHandler;

    @PostMapping 
    public ApiResponse<Void> receive(@RequestBody OrderEvent event){
        orderEventHandler.handle(event);
        return ApiResponse.ok();
    }
}
