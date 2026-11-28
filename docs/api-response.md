# 공통 응답·에러 코드 규칙

모든 서버의 API는 같은 모양으로 응답한다. 앱(Flutter)과 웹(React)이 해석 코드를 하나만 갖게 하기 위해서다.

## 1. 응답 모양

```json
// 성공
{ "success": true, "data": { ... }, "error": null }

// 실패
{
  "success": false,
  "data": null,
  "error": {
    "code": "ORDER_NOT_FOUND",
    "message": "주문을 찾을 수 없습니다.",
    "fieldErrors": [ { "field": "name", "reason": "메뉴 이름은 필수입니다." } ]
  }
}
```

- `fieldErrors`는 입력값 검증 실패일 때만 나온다 (비어 있으면 JSON에서 빠짐)
- 클라이언트는 `success`로 성공·실패를 나누고, 실패면 `error.code`로 분기한다. `message`는 화면에 그대로 보여 줘도 되는 문장이다

## 2. 컨트롤러에서 쓰기

```java
return ApiResponse.ok(data);     // 데이터가 있는 성공
return ApiResponse.ok();         // 데이터가 없는 성공 (삭제 등)
```

- `ApiResponse`는 **본문 모양만** 정한다. 상태 코드는 따로 정한다
  - 기본 200
  - 생성 API는 메서드에 `@ResponseStatus(HttpStatus.CREATED)` (201)
  - 실패 응답의 상태 코드는 에러 코드가 정한다 (아래 3)
- 실패는 `ApiResponse.fail(...)`을 직접 쓰지 않고 **예외를 던진다**. 응답은 공통 예외 처리기가 만든다
- **예외: SSE**(`text/event-stream`)는 감싸지 않고 이벤트를 그대로 보낸다

## 3. 에러 코드

### 만들기

`common`에는 `ErrorCode` 인터페이스만 있고, 실제 코드는 각 서버에서 도메인별 enum으로 만든다. `common`은 고치지 않는다.

```java
// customer-server/.../exception/OrderErrorCode.java
@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
    ORDER_NOT_CANCELABLE(HttpStatus.CONFLICT, "접수 대기 중인 주문만 취소할 수 있습니다.");

    private final HttpStatus status;
    private final String message;
}
```

- 이름: `도메인_이유` (예: `ORDER_`, `CART_`, `MENU_`, `STORE_`). enum 이름이 그대로 응답의 `code`가 된다
- 위치: 그 서버의 `exception/` 폴더. 여러 서버가 같이 쓰는 코드는 `core-domain`에
- 메시지: 사용자에게 보여 줄 문장 (존댓말, 마침표)

### 던지기

```java
throw new BusinessException(OrderErrorCode.ORDER_NOT_FOUND);
throw new BusinessException(CommonErrorCode.COMMON_INVALID_INPUT, "수량은 1~99개");   // 메시지만 바꾸기
```

클라이언트가 **코드로 구분해야 하는 실패**라면 메시지만 바꾸지 말고 코드를 새로 만든다.

### 상태 코드 고르기

| 상태 | 언제                               | 예                                                   |
| ---- | ---------------------------------- | ---------------------------------------------------- |
| 400  | 입력값이 틀림                      | 수량 범위, 형식 오류, 최소 주문 금액 미달            |
| 401  | 로그인이 안 됨 / 서버 간 키가 틀림 | 토큰 없음·만료                                       |
| 403  | 로그인은 됐는데 권한이 없음        | 남의 가게 메뉴 수정                                  |
| 404  | 없음                               | 없는 주문                                            |
| 409  | 지금 상태와 충돌                   | 이미 접수된 주문 취소, 동시 수정, 다른 가게 장바구니 |

**남의 데이터:** 고객 데이터(주소·주문)는 남의 것이면 **404**로 숨긴다 (존재 여부를 알려 주지 않음). 업주의 가게·메뉴처럼 원래 공개된 데이터는 **403**.

### 공통 코드 (`CommonErrorCode`)

| 코드                        | 상태 | 쓰임                                          |
| --------------------------- | ---- | --------------------------------------------- |
| `COMMON_INVALID_INPUT`      | 400  | 입력값 오류 (검증 실패 포함)                  |
| `COMMON_INVALID_JSON`       | 400  | 요청 본문을 읽을 수 없음                      |
| `COMMON_UNAUTHORIZED`       | 401  | 로그인 필요, 서버 간 키 오류                  |
| `COMMON_FORBIDDEN`          | 403  | 권한 없음                                     |
| `COMMON_NOT_FOUND`          | 404  | 없는 주소·데이터                              |
| `COMMON_METHOD_NOT_ALLOWED` | 405  | 지원하지 않는 요청 방식                       |
| `COMMON_CONFLICT`           | 409  | 상태 충돌                                     |
| `COMMON_INTERNAL_ERROR`     | 500  | 예상 못한 오류 (내부 정보는 응답에 넣지 않음) |

## 4. 예외 처리기가 자동으로 처리하는 것

`GlobalExceptionHandler`(common)가 아래를 같은 모양으로 바꿔 준다. 컨트롤러에서 따로 잡지 않는다.

| 상황                                                         | 응답                                         |
| ------------------------------------------------------------ | -------------------------------------------- |
| `BusinessException`                                          | 그 에러 코드의 상태·메시지 (로그 warn 한 줄) |
| `@Valid @RequestBody` 검증 실패                              | 400 + `fieldErrors`                          |
| `@RequestParam`·`@PathVariable` 검증 실패, 누락, 타입 불일치 | 400                                          |
| JSON 문법 오류                                               | 400 `COMMON_INVALID_JSON`                    |
| 없는 주소                                                    | 404                                          |
| 지원하지 않는 메서드                                         | 405                                          |
| 그 밖의 모든 예외                                            | 500 (로그 error에 스택 전체)                 |

## 5. 필터에서 나는 오류

시큐리티 필터는 컨트롤러보다 앞이라 위 처리기가 잡지 못한다. 필터에서 실패 응답을 쓸 때는 `SecurityErrorResponder`를 쓴다 (같은 `ApiResponse` 모양으로 직렬화).

```java
errorResponder.write(response, CommonErrorCode.COMMON_UNAUTHORIZED, "내부 API 키가 올바르지 않습니다.");
```
