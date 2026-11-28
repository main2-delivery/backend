package com.main2.delivery.owner.dto.review;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter 
@Builder 
public class ReviewPageResponse {
    private List<ReviewResponse> reviews;
    private int page;
    private boolean hasNext;

    public ReviewPageResponse(List<ReviewResponse> reviews, int page, boolean hasNext) {
        this.reviews = reviews;
        this.page = page;
        this.hasNext = hasNext;
    }
}
