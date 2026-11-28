# 서버 간 주문 이벤트 (Sprint 1: REST → Sprint 2: RabbitMQ)

다른 서버에 알릴 일은 `OrderEvent` 하나로 보낸다. 본문 모양이 곧 Sprint 2의 MQ 메시지 모양이다.

## 1. 구성

| 위치        | 파일                                                      | 역할                                                |
| ----------- | --------------------------------------------------------- | --------------------------------------------------- |
| core-domain | `core/event/OrderEvent`, `OrderEventType`                 | 메시지                                              |
| core-domain | `core/event/OrderEventNotifier`                           | 보내기 인터페이스. 서비스·리스너는 이것만 의존      |
| common      | `internal/InternalApiKeyFilter`, `InternalSecurityConfig` | `/internal/**`은 `X-Internal-Api-Key`가 맞아야 통과 |
| 받는 서버   | `controller/internal/…Controller` → `OrderEventHandler`   | 컨트롤러는 핸들러만 부르고, 처리는 핸들러에         |

## 2. 메시지

```json
{
  "eventId": "uuid",
  "eventType": "ORDER_CREATED",
  "occurredAt": "2026-11-28T09:00:00Z",
  "orderId": 1001,
  "storeId": 12,
  "userId": 5,
  "status": "PENDING",
  "payload": {}
}
```

- `eventId`: 재시도로 두 번 와도 한 번만 처리하기 위한 번호. 받는 쪽은 처리 기록으로 중복을 거른다
- `occurredAt`: `Instant`(UTC, 끝에 `Z`). 화면에 보여 줄 때만 `Asia/Seoul`로 바꾼다
- `payload`: 종류별 추가 값 (취소·거절은 `reason`, 접수는 `cookingMinutes`)

## 3. 보내기

1. 트랜잭션 안에서 발행한다
   ```java
   eventPublisher.publishEvent(OrderEvent.created(order));
   ```
2. 커밋된 뒤에만 다른 서버로 보낸다 (롤백되면 보내지 않음)
   ```java
   @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
   public void onOrderEvent(OrderEvent event) {
       orderEventNotifier.send(event);
   }
   ```
3. 전송 실패는 예외를 던지지 않고 로그만 남긴다. 주문·상태 변경은 이미 끝났다

## 4. 받기

```java
@PostMapping("/internal/order-events")
public ApiResponse<Void> receive(@RequestBody OrderEvent event) {
    orderEventHandler.handle(event);   // Sprint 2에는 @RabbitListener가 같은 메서드를 부른다
    return ApiResponse.ok();
}
```

- 처리는 `OrderEventHandler`의 `switch`에 종류별로 추가한다. 컨트롤러는 손대지 않는다
- 핸들러는 `HttpServletRequest` 같은 HTTP 객체를 쓰지 않는다 (MQ 리스너에서도 그대로 불려야 하므로)
- `/internal/**` 키 검사는 common 필터가 한다. 서버별로 인터셉터를 따로 만들지 않는다

## 5. 새 이벤트 추가

1. `OrderEventType`에 종류 추가, 필요하면 `OrderEvent`에 만들기 메서드 추가 (`OrderEvent.accepted(order)` 등)
2. 보내는 서버: 3번 순서대로
3. 받는 서버: `OrderEventHandler`의 `switch`에 처리 추가

## 6. 설정

| 키                           | 뜻                                                                                                                        |
| ---------------------------- | ------------------------------------------------------------------------------------------------------------------------- |
| `internal.api-key`           | 서버 간 공유 키. 모든 서버가 같은 값 (`.env`의 `INTERNAL_API_KEY`). **비어 있으면 그 서버의 `/internal/**`은 전부 401\*\* |
| `internal.owner-base-url`    | 고객 서버가 이벤트를 보낼 업주 서버 주소                                                                                  |
| `internal.customer-base-url` | 업주 서버가 이벤트를 보낼 고객 서버 주소                                                                                  |

## 7. 직접 확인

```bash
curl -X POST http://localhost:8082/internal/order-events \
  -H "Content-Type: application/json" -H "X-Internal-Api-Key: local-internal-key" \
  -d '{"eventId":"t-1","eventType":"ORDER_CREATED","occurredAt":"2026-11-28T09:00:00Z","orderId":1,"storeId":1,"userId":1,"status":"PENDING","payload":{}}'
```

업주 서버 로그에 `주문 이벤트 수신 eventId=t-1 ...`이 찍히면 정상. 키를 빼면 401.

## 8. 지금 남아 있는 옛 방식 (Sprint 2에 정리)

- 고객 서버 → 업주 서버: `OwnerOrderNotifier`가 아직 옛 주소(`/internal/orders/created`·`/canceled`)로 보낸다. 업주 서버에 받는 곳이 없어 실패 로그만 남는다 (주문은 정상)
- 업주 서버 → 고객 서버(주문 상태 추적, #21): 고객 서버의 `dto/internal/OrderEvent`·`/internal/orders/status-changed`·`InternalApiKeyInterceptor`를 쓰고 있다. core `OrderEvent`·`/internal/order-events`·common 필터로 맞출 예정
- **업주 접수·거절 기능은 이 정리가 끝난 뒤 core `OrderEvent`로 보낸다**
