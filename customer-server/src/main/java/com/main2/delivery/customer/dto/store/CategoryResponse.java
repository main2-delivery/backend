package com.main2.delivery.customer.dto.store;

import com.main2.delivery.core.entity.StoreCategoryEntity;

import lombok.Getter;

@Getter
public class CategoryResponse {

    private Long categoryId;
    private String categoryName;

    public CategoryResponse(StoreCategoryEntity category) {
        this.categoryId = category.getId();
        this.categoryName = category.getCategoryName();
    }
}