package com.main2.delivery.customer.client;

import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.main2.delivery.customer.event.OrderCreateEvent;

import lombok.extern.slf4j.Slf4j;

// 업주 서버로 주문 알림 (Sprint 1: REST, Sprint 2: RabbitMQ로 교체 예정)
@Slf4j 
@Component 
public class OwnerOrderNotifier {
    private final RestClient restClient;

    public OwnerOrderNotifier(
            @Value("${internal.owner-base-url}") String ownerBaseUrl,
            @Value("${internal.api-key}") String apiKey){
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));   // 업주 서버가 죽어있어도 오래 기다리지 않게
        factory.setReadTimeout(Duration.ofSeconds(2));
        this.restClient = RestClient.builder()
                    .baseUrl(ownerBaseUrl)
                    .defaultHeader("X-Internal-Api-Key", apiKey)    // 헤더 이름은 DUDU-8 합의에 맞춰 수정
                    .requestFactory(factory)
                    .build();
    }

    // 주문 생성 알림. 실패해도 예외를 밖으로 던지지 않음 (주문은 이미 완료됨)
    public void  notfyCreated(OrderCreateEvent event){
        try{
            restClient.post()
                    .uri("/internal/orders/created")
                    .body(Map.of("orderId", event.getOrderId(), "storeId", event.getStoreId()))
                    .retrieve()
                    .toBodilessEntity();
        }catch(Exception e){
            log.warn("업주 서버 주문 알림 실패 orderId={} : {}", event.getOrderId(), e.getMessage());
        }
    }
}
