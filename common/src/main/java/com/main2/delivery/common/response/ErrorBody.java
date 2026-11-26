package com.main2.delivery.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

public record ErrorBody(
        String code,
        String message,
        @JsonInclude(JsonInclude.Include.NON_EMPTY) 
        List<FieldErrorDetail> fieldErrors
    ) {
    public record FieldErrorDetail(String field, String reason) {
    }
}
