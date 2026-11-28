package com.main2.delivery.owner.controller.menu;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.main2.delivery.common.response.ApiResponse;
import com.main2.delivery.owner.dto.menu.MenuCreateResponse;
import com.main2.delivery.owner.dto.menu.MenuFlagRequest;
import com.main2.delivery.owner.dto.menu.MenuRequest;
import com.main2.delivery.owner.dto.menu.MenuResponse;
import com.main2.delivery.owner.service.menu.MenuService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/owner")
@RequiredArgsConstructor 
public class MenuController {
    
    private final MenuService menuService;

    @GetMapping("/stores/{storeId}/menus")
    public ApiResponse<List<MenuResponse>> findMenus(
        @RequestHeader("X-Test-User-Id") Long ownerId,
        @PathVariable Long storeId
    ){
        return ApiResponse.ok(menuService.findMenus(ownerId, storeId));
    }

    @PostMapping("/stores/{storeId}/menus")
    public ResponseEntity<ApiResponse<MenuCreateResponse>> create(
        @RequestHeader("X-Test-User-Id") Long ownerId,
        @PathVariable Long storeId,
        @Valid @RequestBody MenuRequest req
    ){
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(menuService.create(ownerId, storeId, req)));
    }

    @GetMapping("/menus/{menuId}")
    public ApiResponse<MenuResponse> findMenu(
        @RequestHeader("X-Test-User-Id") Long ownerId,
        @PathVariable Long menuId
    ){
        return ApiResponse.ok(menuService.findMenu(ownerId, menuId));
    }

    @PutMapping("/menus/{menuId}")
    public ApiResponse<MenuResponse> updateMenu(
        @RequestHeader("X-Test-User-Id") Long ownerId,
        @PathVariable Long menuId,
        @Valid @RequestBody MenuRequest req
    ){
        return ApiResponse.ok(menuService.updateMenu(ownerId, menuId, req));
    }

    @DeleteMapping("/menus/{menuId}")
    public ApiResponse<Void> deleteMenu(
        @RequestHeader("X-Test-User-Id") Long ownerId,
        @PathVariable Long menuId
    ){
        menuService.deleteMenu(ownerId, menuId);
        return ApiResponse.ok();
    }

    @PatchMapping("/menus/{menuId}/sold-out")
    public ApiResponse<MenuResponse> changeSoldOut(
        @RequestHeader("X-Test-User-Id") Long ownerId,
        @PathVariable Long menuId,
        @Valid @RequestBody MenuFlagRequest req
    ){
        return ApiResponse.ok(menuService.changeSoldOut(ownerId, menuId, req));
    }

    @PatchMapping("/menus/{menuId}/hidden")
    public ApiResponse<MenuResponse> changeHidden(
        @RequestHeader("X-Test-User-Id") Long ownerId,
        @PathVariable Long menuId,
        @Valid @RequestBody MenuFlagRequest req
    ){
        return ApiResponse.ok(menuService.changeHidden(ownerId, menuId, req));
    }

    @PatchMapping("/menus/{menuId}/representative")
    public ApiResponse<MenuResponse> changeRepresentative(
        @RequestHeader("X-Test-User-Id") Long ownerId,
        @PathVariable Long menuId,
        @Valid @RequestBody MenuFlagRequest req
    ){
        return ApiResponse.ok(menuService.changeRepresentative(ownerId, menuId, req));
    }

    @PostMapping("/menus/{menuId}/copy")
    public ResponseEntity<ApiResponse<MenuCreateResponse>> copyMenu(
        @RequestHeader("X-Test-User-Id") Long ownerId,
        @PathVariable Long menuId
    ){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(menuService.copyMenu(ownerId, menuId)));
    }

}
