package com.main2.delivery.customer.controller.address;

import java.util.List;

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
import com.main2.delivery.customer.dto.address.UserAddressRequest;
import com.main2.delivery.customer.dto.address.UserAddressResponse;
import com.main2.delivery.customer.service.address.UserAddressService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class UserAddressController {

    private final UserAddressService addressService;

    // [1] 내 주소 목록
    @GetMapping
    public ApiResponse<List<UserAddressResponse>> findMyAddresses(
            @RequestHeader("X-Test-User-Id") Long userId) {
        return ApiResponse.ok(addressService.findMyAddresses(userId));
    }

    // [2] 주소 등록
    @PostMapping
    public ApiResponse<UserAddressResponse> create(
            @RequestHeader("X-Test-User-Id") Long userId,
            @RequestBody UserAddressRequest req) {
        return ApiResponse.ok(addressService.create(userId, req));
    }

    // [3] 주소 수정
    @PutMapping("/{addressId}")
    public ApiResponse<UserAddressResponse> update(
            @RequestHeader("X-Test-User-Id") Long userId,
            @PathVariable Long addressId,
            @RequestBody UserAddressRequest req) {
        return ApiResponse.ok(addressService.update(userId, addressId, req));
    }

    // [4] 주소 삭제
    @DeleteMapping("/{addressId}")
    public ApiResponse<Void> delete(
            @RequestHeader("X-Test-User-Id") Long userId,
            @PathVariable Long addressId) {
        addressService.delete(userId, addressId);
        return ApiResponse.ok();
    }

    // [5] 기본 주소 변경
    @PutMapping("/{addressId}/default")
    public ApiResponse<UserAddressResponse> changeDefault(
            @RequestHeader("X-Test-User-Id") Long userId,
            @PathVariable Long addressId) {
        return ApiResponse.ok(addressService.changeDefault(userId, addressId));
    }
}