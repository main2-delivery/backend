package com.main2.delivery.customer.controller.cart;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.main2.delivery.common.response.ApiResponse;
import com.main2.delivery.customer.dto.cart.CartAddRequest;
import com.main2.delivery.customer.dto.cart.CartQuantityRequest;
import com.main2.delivery.customer.dto.cart.CartResponse;
import com.main2.delivery.customer.service.cart.CartService;

import lombok.RequiredArgsConstructor;

@RestController 
@RequestMapping ("/api/cart")
@RequiredArgsConstructor 
public class CartController {
    private final CartService cartService;

    // [1] 장바구니 조회
    @GetMapping 
    public ApiResponse<CartResponse> getCart(@RequestHeader("X-Test-User-Id") Long userId ){
        return ApiResponse.ok(cartService.getCart(userId));
    }

    // [2] 담기
    @PostMapping("/items")
    public ApiResponse<CartResponse> addItem(
            @RequestHeader("X-Test-User-Id") Long userId,
            @RequestBody CartAddRequest req){
        return ApiResponse.ok(cartService.addItem(userId, req));            
    }

    // [3] 수량 변경
    @PutMapping("/items/{cartItemId}")
    public ApiResponse<CartResponse> changeQuantity(
            @RequestHeader("X-Test-User-Id") Long userId,
            @PathVariable String cartItemId,
            @RequestBody CartQuantityRequest req){
        return ApiResponse.ok(cartService.changeQuantity(userId, cartItemId, req));
    }

    // [4] 항목 삭제
    @DeleteMapping("/items/{cartItemId}")
    public ApiResponse<CartResponse> removeItem(
            @RequestHeader("X-Test-User-Id") Long userId,
            @PathVariable String cartItemId){
        return ApiResponse.ok(cartService.removeItem(userId, cartItemId));
    }

    // [5] 전체 비우기
    @DeleteMapping
    public ApiResponse<Void> clear(
            @RequestHeader("X-Test-User-Id") Long userId){
        cartService.clear(userId);
        return ApiResponse.ok();
    }
}
