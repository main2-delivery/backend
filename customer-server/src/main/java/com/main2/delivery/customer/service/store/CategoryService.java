package com.main2.delivery.customer.service.store;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.main2.delivery.core.entity.StoreCategoryEntity;
import com.main2.delivery.customer.dto.store.CategoryResponse;
import com.main2.delivery.customer.repository.store.StoreCategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final StoreCategoryRepository categoryRepository;

    // 카테고리 목록 (데이터가 없으면 빈 목록 → 앱은 '전체'만 표시)
    public List<CategoryResponse> findCategories() {
        List<CategoryResponse> result = new ArrayList<>();
        for (StoreCategoryEntity category : categoryRepository.findAllByOrderBySortOrderAscIdAsc()) {
            result.add(new CategoryResponse(category));
        }
        return result;
    }
}