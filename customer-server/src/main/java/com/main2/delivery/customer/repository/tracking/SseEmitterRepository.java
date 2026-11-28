package com.main2.delivery.customer.repository.tracking;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

// 주문별 SSE 연결 보관 (서버 메모리)
// 한 주문을 여러 기기에서 볼 수 있어서 List. 여러 스레드가 동시에 넣고 빼서 동시성 컬렉션 사용
@Repository 
public class SseEmitterRepository {
    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    // 연결 추가
    public void add(Long orderId, SseEmitter emitter){
        emitters.computeIfAbsent(orderId, key -> new CopyOnWriteArrayList<>()).add(emitter);
    }

    // 이 주문을 구독 중인 연결들
    public List<SseEmitter> findByOrderId(Long orderId){
        return emitters.getOrDefault(orderId, List.of());
    }

    // 전체 (하트비트용)
    public Map<Long, List<SseEmitter>> findAll(){
        return emitters;
    }

    // 연결 하나 제거 (마지막이면 주문 키도 지움)
    public void remove(Long orderId, SseEmitter emitter){
        emitters.computeIfPresent(orderId, (key, list) -> {
            list.remove(emitter);
            return list.isEmpty() ? null : list;
        });
    }

    // 이 주문의 연결 전부 제거
    public void removeAll(Long orderId){
        emitters.remove(orderId);
    }
}
