package com.main2.delivery.customer.controller.store;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.main2.delivery.common.response.ApiResponse;
import com.main2.delivery.customer.dto.store.CategoryResponse;
import com.main2.delivery.customer.service.store.CategoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/customer/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // 홈 상단 카테고리 탭
    @GetMapping
    public ApiResponse<List<CategoryResponse>> findCategories() {
        return ApiResponse.ok(categoryService.findCategories());
    }
}