package com.main2.delivery.owner.dto.review;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.main2.delivery.core.entity.ReviewEntity;

import lombok.Getter;

@Getter 
public class ReviewResponse {
    private Long reviewId;
    private String nickname;         // user.nickname, 수집 리뷰나 닉네임이 없으면 null
    private BigDecimal rating;       // 0.5 단위, null 가능
    private String content;          // DB의 body
    private LocalDateTime createdAt;
    private String reply;            // null이면 미답변
    private LocalDateTime repliedAt;

    public ReviewResponse(ReviewEntity entity) { 
        
        // 현재 데이터에 user_id가 null 또는 user_nickname이 null 인 경우 익명으로
        this.nickname = (entity.getUser() != null && entity.getUser().getNickname() != null)
        ? entity.getUser().getNickname()
        : "익명";

        this.reviewId = entity.getId();
        // this.nickname = entity.getUser().getNickname();
        this.rating = entity.getRating();
        this.content = entity.getBody();
        this.createdAt = entity.getCreatedAt();
        this.reply = entity.getReply();
        this.repliedAt = entity.getRepliedAt();
    }

    
}