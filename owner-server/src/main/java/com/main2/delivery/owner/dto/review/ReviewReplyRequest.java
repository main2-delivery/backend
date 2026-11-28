package com.main2.delivery.owner.dto.review;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@NoArgsConstructor 
public class ReviewReplyRequest {

    @NotBlank (message = "답글을 입력해 주세요.")
    @Size (max = 1000, message = "답글은 1000자 이하입니다.")
    private String reply;
}