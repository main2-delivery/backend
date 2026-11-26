package com.main2.delivery.common.geocode;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

// 카카오 로컬 API 로 주소를 위경도로 바꾼다 (키가 없으면 변환하지 않고 빈 값)
@Slf4j
@Service
public class GeoCodingService {

    private static final String KAKAO_LOCAL_URL = "https://dapi.kakao.com";

    private final String apiKey;
    private final RestClient restClient;

    public GeoCodingService(@Value("${kakao.rest-api-key:}") String apiKey) {
        this.apiKey = apiKey;
        this.restClient = RestClient.builder().baseUrl(KAKAO_LOCAL_URL).build();
    }

    public Optional<GeoPoint> geocode(String address) {
        if (apiKey.isBlank()) {
            log.warn("kakao.rest-api-key 가 비어 있어 주소를 좌표로 바꾸지 못했어요");
            return Optional.empty();
        }
        if (address == null || address.isBlank()) {
            return Optional.empty();
        }
        try {
            KakaoAddressResponse response = restClient.get()
                    .uri(uri -> uri.path("/v2/local/search/address.json").queryParam("query", address.trim()).build())
                    .header("Authorization", "KakaoAK " + apiKey)
                    .retrieve()
                    .body(KakaoAddressResponse.class);
            if (response == null || response.getDocuments().isEmpty()) {
                return Optional.empty();
            }
            KakaoAddressResponse.Document first = response.getDocuments().get(0);
            BigDecimal lat = new BigDecimal(first.getY()).setScale(7, RoundingMode.HALF_UP);
            BigDecimal lng = new BigDecimal(first.getX()).setScale(7, RoundingMode.HALF_UP);
            return Optional.of(new GeoPoint(lat, lng));
        } catch (RestClientException | NumberFormatException e) {
            log.warn("주소 좌표 변환 실패: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
