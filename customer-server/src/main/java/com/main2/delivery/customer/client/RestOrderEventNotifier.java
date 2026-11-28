package com.main2.delivery.customer.client;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.main2.delivery.core.event.OrderEvent;
import com.main2.delivery.core.event.OrderEventNotifier;

import lombok.extern.slf4j.Slf4j;

// 고객 서버 → 업주 서버 주문 이벤트 (Sprint 1: REST / Sprint 2(#57): RabbitMQ 구현체로 교체)
// 서비스·리스너는 OrderEventNotifier 인터페이스만 앎
@Slf4j 
@Component 
public class RestOrderEventNotifier implements OrderEventNotifier{
    private final RestClient restClient;

    public RestOrderEventNotifier(
            RestClient.Builder builder,     // 스프링이 만든 빌더 -> 날짜(Instant) 직렬화 설정이 서버와 같음
            @Value("${internal.owner-base-url}") String ownerBaseUrl,
            @Value("${internal.api-key}") String apiKey){
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));   // 업주 서버가 죽어 있어도 오래 기다리지 않게
        factory.setReadTimeout(Duration.ofSeconds(2));
        this.restClient = builder
                .baseUrl(ownerBaseUrl)
                .defaultHeader("X-Internal-Api-Key", apiKey)
                .requestFactory(factory)
                .build();
    }

    // 실패해도 예외를 던지지 않음 (주문/취소는 이미 커밋됨)
    @Override 
    public void send(OrderEvent event){
        try{
            restClient.post()
                    .uri("/internal/order-events")
                    .body(event)
                    .retrieve()
                    .toBodilessEntity();
        }catch(Exception e){
            log.warn("업주 서버 이벤트 전송 실패 eventId={} type={} orderId={} : {}", event.eventId(), event.eventType(), event.orderId(), e.getMessage());
        }
    }
}
