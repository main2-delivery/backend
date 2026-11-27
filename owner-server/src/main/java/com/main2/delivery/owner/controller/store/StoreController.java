package com.main2.delivery.owner.controller.store;

import com.main2.delivery.common.exception.BusinessException;
import com.main2.delivery.common.exception.CommonErrorCode;
import com.main2.delivery.common.response.ApiResponse;
import com.main2.delivery.owner.dto.store.StoreCreateRequestDto;
import com.main2.delivery.owner.dto.store.StoreCreateResponseDto;
import com.main2.delivery.owner.dto.store.StoreDetailResponseDto;
import com.main2.delivery.owner.dto.store.StoreSummaryResponseDto;
import com.main2.delivery.owner.dto.store.StoreUpdateRequestDto;
import com.main2.delivery.owner.exception.OwnerErrorCode;
import com.main2.delivery.owner.service.store.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 업주 가게 API: 입점 신청, 내 가게 목록, 가게 정보 조회·수정
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/owner/stores")
public class StoreController {

    private final StoreService storeService;

    // 1. 입점 신청
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<StoreCreateResponseDto> apply(@RequestHeader(value = "X-Test-User-Id", required = false) Long userId,
                                                  @RequestHeader(value = "X-Test-Role", required = false) String role,
                                                  @RequestBody StoreCreateRequestDto request) {
        return ApiResponse.ok(storeService.apply(loginOwnerId(userId, role), request));
    }

    // 2. 내 가게 목록
    @GetMapping
    public ApiResponse<List<StoreSummaryResponseDto>> getMyStores(@RequestHeader(value = "X-Test-User-Id", required = false) Long userId,
                                                               @RequestHeader(value = "X-Test-Role", required = false) String role) {
        return ApiResponse.ok(storeService.getMyStores(loginOwnerId(userId, role)));
    }

    // 3. 가게 정보 조회
    @GetMapping("/{storeId}")
    public ApiResponse<StoreDetailResponseDto> getMyStore(@RequestHeader(value = "X-Test-User-Id", required = false) Long userId,
                                                       @RequestHeader(value = "X-Test-Role", required = false) String role,
                                                       @PathVariable Long storeId) {
        return ApiResponse.ok(storeService.getMyStore(loginOwnerId(userId, role), storeId));
    }

    // 4. 가게 정보 수정
    @PutMapping("/{storeId}")
    public ApiResponse<StoreDetailResponseDto> updateMyStore(@RequestHeader(value = "X-Test-User-Id", required = false) Long userId,
                                                          @RequestHeader(value = "X-Test-Role", required = false) String role,
                                                          @PathVariable Long storeId,
                                                          @RequestBody StoreUpdateRequestDto request) {
        return ApiResponse.ok(storeService.updateMyStore(loginOwnerId(userId, role), storeId, request));
    }

    // 테스트 헤더로 로그인 업주 확인 (인증 작업 DUDU-32 후 JWT 쿠키로 교체)
    private Long loginOwnerId(Long userId, String role) {
        if (userId == null) {
            throw new BusinessException(CommonErrorCode.COMMON_UNAUTHORIZED);
        }
        if (!"OWNER".equals(role)) {
            throw new BusinessException(OwnerErrorCode.OWNER_ROLE_REQUIRED);
        }
        return userId;
    }
}
