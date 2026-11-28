package com.main2.delivery.owner.controller.store;

import com.main2.delivery.common.exception.BusinessException;
import com.main2.delivery.common.exception.CommonErrorCode;
import com.main2.delivery.common.response.ApiResponse;
import com.main2.delivery.common.security.LoginUser;
import com.main2.delivery.owner.dto.store.StoreCreateRequestDto;
import com.main2.delivery.owner.dto.store.StoreCreateResponseDto;
import com.main2.delivery.owner.dto.store.StoreDetailResponseDto;
import com.main2.delivery.owner.dto.store.StoreSummaryResponseDto;
import com.main2.delivery.owner.dto.store.StoreUpdateRequestDto;
import com.main2.delivery.owner.exception.OwnerErrorCode;
import com.main2.delivery.owner.service.store.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    public ApiResponse<StoreCreateResponseDto> apply(@AuthenticationPrincipal LoginUser user,
                                                  @RequestBody StoreCreateRequestDto request) {
        return ApiResponse.ok(storeService.apply(loginOwnerId(user), request));
    }

    // 2. 내 가게 목록
    @GetMapping
    public ApiResponse<List<StoreSummaryResponseDto>> getMyStores(@AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok(storeService.getMyStores(loginOwnerId(user)));
    }

    // 3. 가게 정보 조회
    @GetMapping("/{storeId}")
    public ApiResponse<StoreDetailResponseDto> getMyStore(@AuthenticationPrincipal LoginUser user,
                                                  @PathVariable Long storeId) {
        return ApiResponse.ok(storeService.getMyStore(loginOwnerId(user), storeId));
    }

    // 4. 가게 정보 수정
    @PutMapping("/{storeId}")
    public ApiResponse<StoreDetailResponseDto> updateMyStore(@AuthenticationPrincipal LoginUser user,
                                                  @PathVariable Long storeId,
                                                          @RequestBody StoreUpdateRequestDto request) {
        return ApiResponse.ok(storeService.updateMyStore(loginOwnerId(user), storeId, request));
    }

    // 로그인 업주 id. 고객·업주 id 가 겹치므로 역할을 꼭 같이 확인한다 (docs/auth.md)
    // #32 이후에는 /api/owner/** 주소 규칙으로 업주만 들어오므로 역할 확인은 지워도 된다
    private Long loginOwnerId(LoginUser user) {
        if (user == null) {
            throw new BusinessException(CommonErrorCode.COMMON_UNAUTHORIZED);
        }
        if (!user.isOwner()) {
            throw new BusinessException(OwnerErrorCode.OWNER_ROLE_REQUIRED);
        }
        return user.id();
    }
}
