package com.main2.delivery.customer.service.tracking;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.main2.delivery.common.exception.BusinessException;
import com.main2.delivery.core.entity.OrderEntity;
import com.main2.delivery.core.entity.OrderStatus;
import com.main2.delivery.customer.dto.tracking.OrderStatusMessage;
import com.main2.delivery.customer.dto.tracking.TrackingResponse;
import com.main2.delivery.customer.exception.OrderErrorCode;
import com.main2.delivery.customer.repository.order.OrderRepository;
import com.main2.delivery.customer.repository.tracking.SseEmitterRepository;
import com.main2.delivery.customer.repository.tracking.TrackingRedisRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j 
@Service 
@RequiredArgsConstructor 
public class TrackingService {
    private static final long TIMEOUT_MS = 30 * 60 * 1000L; // 연결 유지 30분 (끊기면 앱이 다시 구독)
    private static final String EVENT_NAME = "order-status";
    // 더 이상 바뀌지 않는 상태 -> 보내고 연결 정리
    private static final Set<OrderStatus> FINISHED = EnumSet.of(OrderStatus.DELIVERED, OrderStatus.REJECTED, OrderStatus.CANCELED);
    
    private final OrderRepository orderRepository;
    private final TrackingRedisRepository trackingRedisRepository;
    private final SseEmitterRepository sseEmitterRepository;

    // [1] 현재 추적 정보 (추적 화면 진입 시 한 번)
    @Transactional (readOnly = true)
    public TrackingResponse getTracking(Long userId, Long orderId){
        OrderEntity order = findMyOrder(userId, orderId);
        return new TrackingResponse(order, trackingRedisRepository.findEstimatedReadyAt(orderId));
    }

    // [2] SSE 구독. 연결 직후 현재 상태를 한번 보내 재연결해도 화면이 일치하게
    public SseEmitter subscribe(Long userId, Long orderId){
        OrderEntity order = findMyOrder(userId, orderId);   // 여기서 DB 커넥션 반납(OSIV off)
        OrderStatusMessage current = new OrderStatusMessage(orderId, order.getStatus().name(), null, trackingRedisRepository.findEstimatedReadyAt(orderId), order.getCancelReason());
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);

        // 이미 끝난 주문: 현재 상태만 주고 닫음
        if(FINISHED.contains(order.getStatus())){
            send(emitter, current);
            emitter.complete();
            return emitter;
        }

        sseEmitterRepository.add(orderId, emitter);
        emitter.onCompletion(()-> sseEmitterRepository.remove(orderId, emitter));   // 정상 종료
        emitter.onTimeout(emitter::complete);                                       // 30분 -> 종료
        emitter.onError(e -> sseEmitterRepository.remove(orderId, emitter));        // 앱이 끊음

        send(emitter, current);
        return emitter;
    }

    // [3] 상태 변경을 이 주문 구독자 전부에게 보냄. 끝난 상태면 연결 정리
    public void push(OrderStatusMessage message){
        Long orderId = message.getOrderId();
        for(SseEmitter emitter : sseEmitterRepository.findByOrderId(orderId)){
            if(!send(emitter, message)){
                sseEmitterRepository.remove(orderId, emitter);  // 보내기 실패 = 끊긴 연결
            }
        }
        if(FINISHED.contains(OrderStatus.valueOf(message.getStatus()))){
            sseEmitterRepository.findByOrderId(orderId).forEach(SseEmitter::complete);
            sseEmitterRepository.removeAll(orderId);
        }
    }

    // [4] 15초마다 빈 신호 -> 중간에서 연결이 끊기지 않게 + 죽은 연결 정리
    @Scheduled (fixedRate = 15_000)
    public void heartbeat(){
        sseEmitterRepository.findAll().forEach((orderId, list) -> 
                    list.forEach((emitter) -> {
                        try{
                            emitter.send(SseEmitter.event().comment("ping"));
                        }catch(IOException | IllegalStateException e){
                            sseEmitterRepository.remove(orderId, emitter);
                        }
                    }));
    }

    // 네 주문 (남의 주문이면 404)
    private OrderEntity findMyOrder(Long userId, Long orderId){
        return orderRepository.findByIdAndUserId(orderId, userId).orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
    }

    // 이벤트 하나 보내기. 실패하면 false
    private boolean send(SseEmitter emitter, OrderStatusMessage message){
        try{
            emitter.send(SseEmitter.event().name(EVENT_NAME).data(message));
            return true;
        }catch(IOException | IllegalStateException e){
            log.debug("SSE 전송 실패 orderId={} : {}", message.getOrderId(), e.getMessage());
            return false;
        }
    }
}
