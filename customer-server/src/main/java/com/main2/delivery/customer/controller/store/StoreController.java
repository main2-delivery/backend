package com.main2.delivery.customer.controller.store;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.main2.delivery.common.response.ApiResponse;
import com.main2.delivery.customer.dto.store.MenuResponse;
import com.main2.delivery.customer.dto.store.StoreDetailResponse;
import com.main2.delivery.customer.dto.store.StoreListResponse;
import com.main2.delivery.customer.service.store.StoreService;

import lombok.RequiredArgsConstructor;

@RestController 
@RequestMapping ("/api/customer/stores")
@RequiredArgsConstructor 
public class StoreController {
    private final StoreService storeService;

    // [1] 배달 가능 가게 목록
    @GetMapping
    public ApiResponse<List<StoreListResponse>> findStores(
            @RequestHeader("X-Test-User-Id") Long userId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(storeService.findStores(userId, categoryId, page, size));
    }
    
    // [2] 가게 상세
    @GetMapping("/{storeId}")
    public ApiResponse<StoreDetailResponse> findStoreDetail(@PathVariable Long storeId) {
        return ApiResponse.ok(storeService.findStoreDetail(storeId));
    }

    // [3] 메뉴판
    @GetMapping("/{storeId}/menus")
    public ApiResponse<List<MenuResponse>> findMenus(@PathVariable Long storeId) {
        return ApiResponse.ok(storeService.findMenus(storeId));
    }
}
