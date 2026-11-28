package com.main2.delivery.customer.dto.order;

import java.util.List;

import org.springframework.data.domain.Page;

import com.main2.delivery.core.entity.OrderEntity;

import lombok.Getter;

@Getter 
public class OrderListResponse {
    private final List<OrderSummaryResponse> orders;
    private final int page;
    private final int size;
    private final long totalElements;
    private final boolean hasNext;  // 앱 무한 스크롤용

    public OrderListResponse(Page<OrderEntity> result, List<OrderSummaryResponse> orders){
        this.orders = orders;
        this.page = result.getNumber();
        this.size = result.getSize();
        this.totalElements = result.getTotalElements();
        this.hasNext = result.hasNext();
    }
}
