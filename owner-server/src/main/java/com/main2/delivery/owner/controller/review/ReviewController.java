package com.main2.delivery.owner.controller.review;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.main2.delivery.common.response.ApiResponse;
import com.main2.delivery.owner.dto.review.ReviewPageResponse;
import com.main2.delivery.owner.dto.review.ReviewReplyRequest;
import com.main2.delivery.owner.dto.review.ReviewResponse;
import com.main2.delivery.owner.service.review.ReviewService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor 
@RequestMapping("/api/owner")
public class ReviewController {
    
    private final ReviewService reviewService;
    
    @GetMapping("/stores/{storeId}/reviews")
    public ApiResponse<ReviewPageResponse> findReviews(
        @RequestHeader("X-Test-User-Id") Long ownerId,
        @PathVariable Long storeId,
        @RequestParam(defaultValue = "false") boolean unanswered,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
    return ApiResponse.ok(reviewService.findReviews(ownerId, storeId, unanswered, pageable));
    }

    @PutMapping("/reviews/{reviewId}/reply")
    public ApiResponse<ReviewResponse> putReply(
        @RequestHeader("X-Test-User-Id") Long ownerId,
        @PathVariable Long reviewId,
        @Valid @RequestBody ReviewReplyRequest req   
    ){
        return ApiResponse.ok(reviewService.putReply(ownerId, reviewId, req));
    }

    @DeleteMapping("/reviews/{reviewId}/reply")
    public ApiResponse<Void> deleteReply(
        @RequestHeader("X-Test-User-Id") Long ownerId,
        @PathVariable Long reviewId
    ){
        reviewService.deleteReply(ownerId, reviewId);
        return ApiResponse.ok();
    }

}