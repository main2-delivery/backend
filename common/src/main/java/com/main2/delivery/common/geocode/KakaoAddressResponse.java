package com.main2.delivery.common.geocode;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

// 카카오 주소 검색 응답 중 좌표만 받는 DTO (x = 경도, y = 위도)
@Data
@NoArgsConstructor
public class KakaoAddressResponse {
    private List<Document> documents = new ArrayList<>();

    @Data
    @NoArgsConstructor
    public static class Document {
        private String x;
        private String y;
    }
}
