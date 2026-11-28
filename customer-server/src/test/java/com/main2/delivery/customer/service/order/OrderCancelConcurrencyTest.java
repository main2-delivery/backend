package com.main2.delivery.customer.service.order;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import com.main2.delivery.core.entity.OrderEntity;
import com.main2.delivery.core.entity.OrderStatus;
import com.main2.delivery.customer.repository.order.OrderRepository;
import com.main2.delivery.customer.repository.store.StoreRepository;
import com.main2.delivery.customer.repository.user.UserRepository;

// 진짜 DB(도커 MySQL)로 돌리는 통합 테스트. 도커가 켜져 있어야 함
// GitHub CI에는 MySQL이 없어서 CI에서는 건너뜀 (로컬에서만 실행)
@SpringBootTest
@DisabledIfEnvironmentVariable(named = "CI", matches = "true")
class OrderCancelConcurrencyTest {

    @Autowired OrderService orderService;
    @Autowired OrderRepository orderRepository;
    @Autowired UserRepository userRepository;
    @Autowired StoreRepository storeRepository;
    @Autowired TransactionTemplate transactionTemplate;

    private static final Long USER_ID = 1L;
    private static final Long STORE_ID = 33167269L;
    private Long orderId;

    // 테스트마다 접수 대기(PENDING) 주문을 새로 하나 만듦
    @BeforeEach
    void setUp() {
        orderId = transactionTemplate.execute(status -> {
            OrderEntity order = OrderEntity.builder()
                    .orderKey("test-" + UUID.randomUUID())
                    .user(userRepository.getReferenceById(USER_ID))
                    .store(storeRepository.getReferenceById(STORE_ID))
                    .menuAmount(26000)
                    .deliveryFee(0)
                    .deliveryAddress("동시성 테스트 주소")
                    .deliveryLat(new BigDecimal("37.3418421"))
                    .deliveryLng(new BigDecimal("126.6866293"))
                    .paymentMethod("CARD")
                    .build();
            return orderRepository.save(order).getId();
        });
    }

    // 테스트로 만든 주문은 지움 (개발 DB를 더럽히지 않게)
    @AfterEach
    void tearDown() {
        orderRepository.deleteById(orderId);
    }

    @Test
    @DisplayName("점주 접수와 고객 취소가 동시에 들어오면 하나만 성공한다")
    void acceptAndCancelAtSameTime() throws InterruptedException {
        int threadCount = 10;   // 접수 5 + 취소 5
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);   // 다 준비될 때까지
        CountDownLatch start = new CountDownLatch(1);             // 출발 신호
        CountDownLatch done = new CountDownLatch(threadCount);    // 다 끝날 때까지

        AtomicInteger acceptSuccess = new AtomicInteger();
        AtomicInteger cancelSuccess = new AtomicInteger();
        AtomicInteger fail = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            boolean isAccept = (i % 2 == 0);
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();  // 출발 신호까지 대기 -> 10개가 동시에 출발
                    if (isAccept) {
                        acceptByOwner(orderId);
                        acceptSuccess.incrementAndGet();
                    } else {
                        orderService.cancelOrder(USER_ID, orderId, null);
                        cancelSuccess.incrementAndGet();
                    }
                } catch (Exception e) {
                    fail.incrementAndGet();     // 409(락 충돌·이미 처리됨) 등
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await();
        start.countDown();      // 출발!
        done.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        OrderEntity result = orderRepository.findById(orderId).orElseThrow();
        System.out.printf("접수 성공=%d, 취소 성공=%d, 실패=%d, 최종 상태=%s, version=%d%n",
                acceptSuccess.get(), cancelSuccess.get(), fail.get(), result.getStatus(), result.getVersion());

        // [1] 딱 하나만 성공
        assertThat(acceptSuccess.get() + cancelSuccess.get()).isEqualTo(1);
        assertThat(fail.get()).isEqualTo(threadCount - 1);
        // [2] 최종 상태가 이긴 쪽과 같음 (꼬이지 않음)
        OrderStatus expected = acceptSuccess.get() == 1 ? OrderStatus.ACCEPTED : OrderStatus.CANCELED;
        assertThat(result.getStatus()).isEqualTo(expected);
        // [3] UPDATE는 한 번만 반영됨
        assertThat(result.getVersion()).isEqualTo(1L);
    }

    // 업주 서버의 '주문 접수'를 흉내 (업주 서버도 core-domain의 같은 OrderEntity·@Version을 씀)
    private void acceptByOwner(Long orderId) {
        transactionTemplate.executeWithoutResult(status -> {
            OrderEntity order = orderRepository.findById(orderId).orElseThrow();
            if (order.getStatus() != OrderStatus.PENDING) {
                throw new IllegalStateException("이미 처리된 주문");
            }
            order.setStatus(OrderStatus.ACCEPTED);
            order.setAcceptedAt(LocalDateTime.now());
        });
    }
}