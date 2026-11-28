# 현재 사용자 받기 (인증 사용법)

## 1. 컨트롤러에서 받기

```java
@GetMapping
public ApiResponse<OrderListResponse> getOrders(@AuthenticationPrincipal LoginUser user) {
    return ApiResponse.ok(orderService.getOrders(user.id()));
}
```

- `LoginUser(Long id, Role role)` — `id`는 역할에 따라 `user_id` 또는 `owner_id`
- 고객·업주 테이블의 id가 겹치므로(고객 3번 ≠ 업주 3번) **id는 항상 역할과 함께** 본다
- 사용자 id를 주소·쿼리·본문·헤더로 직접 받지 않는다. `@RequestHeader("X-Test-User-Id")`는 쓰지 않는다

## 2. 주소 규칙

| 앞부분             | 누가        | 서버                                |
| ------------------ | ----------- | ----------------------------------- |
| `/api/customer/**` | 고객 앱     | customer-server                     |
| `/api/owner/**`    | 업주 웹     | owner-server                        |
| `/internal/**`     | 다른 서버만 | 각 서버 (`docs/internal-events.md`) |

#32(Security 적용) 이후에는 이 앞부분으로 권한을 나눈다. 규칙에 안 맞는 주소는 막힌다.

## 3. 지금(개발용): 테스트 헤더

JWT 로그인이 붙기 전까지는 요청 헤더로 사용자를 정한다. 컨트롤러는 1번처럼 그대로 쓰면 된다.

| 헤더             | 값                                             | 없으면                 |
| ---------------- | ---------------------------------------------- | ---------------------- |
| `X-Test-User-Id` | 1 이상의 숫자                                  | 둘 다 없으면 고객 1번  |
| `X-Test-Role`    | `CUSTOMER` / `OWNER` / `ADMIN` (대소문자 무관) | id만 있으면 `CUSTOMER` |

- 역할만 있고 id가 없거나, id가 숫자가 아니거나 1보다 작거나, 없는 역할이면 400 `COMMON_INVALID_INPUT`
- 켜고 끄기: `application.yml`의 `ddip.auth.dev-header.enabled` (서버 4개 모두 `true`). 설정이 없으면 꺼진다

```bash
curl http://localhost:8082/api/owner/stores -H "X-Test-User-Id: 3" -H "X-Test-Role: OWNER"
```

## 4. 남의 데이터 확인

로그인했다고 끝이 아니다. 서비스에서 **내 것인지** 확인한다.

- 고객: `findByIdAndUserId(id, user.id())`처럼 조회 조건에 사용자 id를 같이 넣는다 → 없으면 404
- 업주: 가게의 `owner`가 `user.id()`와 같은지 확인 → 다르면 403 (수집 가게는 `owner`가 없으므로 누구의 것도 아님)

## 5. 테스트에서

- 서비스 테스트: 사용자 id를 그냥 넘긴다
- `@WebMvcTest`에서 헤더 인증까지 확인하려면 `properties = "ddip.auth.dev-header.enabled=true"`와 `@Import(SecurityConfig.class)`를 붙이고 헤더를 넣어 요청한다
- 인증과 상관없는 컨트롤러 테스트는 `@AutoConfigureMockMvc(addFilters = false)`로 필터를 끈다

## 6. 앞으로 바뀌는 것 (#13·#32)

- 입구만 바뀐다: 테스트 헤더 → 쿠키의 JWT. **컨트롤러 코드는 그대로다** (`@AuthenticationPrincipal LoginUser`)
- 로그인 없이 부르면 401, 권한이 없으면 403 (응답 모양은 `docs/api-response.md`와 같음)
