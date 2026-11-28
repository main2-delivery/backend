# 띱 백엔드

위치 기반 배달 주문·배차 플랫폼. Java 17, Spring Boot 4.1.3, Gradle 멀티 모듈.

## 모듈
| 모듈 | 역할 | 포트 |
|---|---|---|
| common | 공통 응답, 임시 사용자 주입, 서버 간 REST 호출 | - |
| core-domain | 고객·업주 서버가 함께 쓰는 엔티티 | - |
| customer-server | 고객 API | 8081 |
| owner-server | 업주 API | 8082 |
| delivery-server | 배차·배달 (Sprint 2) | 8083 |
| store-server | 매장 관리 시스템 (Sprint 2) | 8084 |

## 실행
1. `docker compose up -d` (MySQL, Redis, Prometheus, Grafana)
2. `./gradlew :customer-server:bootRun`
3. `./gradlew :owner-server:bootRun`

접속 정보 기본값은 `docker-compose.yml`과 맞춰져 있어 `.env` 없이도 로컬에서 실행된다.
값을 바꾸려면 `.env.example`을 `.env`로 복사해 수정한다. `.env`는 올리지 않는다.

### .env 읽기와 카카오 키
- 고객·업주 서버는 `spring.config.import`로 `backend/.env`를 직접 읽는다 (Spring은 `.env`를 자동으로 읽지 않음).
- 주소 → 좌표 변환(`common`의 `GeoCodingService`)은 카카오 REST API 키가 필요하다. 팀 공용 키를 개인 메시지로 받아 `.env`에 넣는다.
  ```
  KAKAO_REST_API_KEY=받은키
  ```
- 키가 비어 있어도 서버는 뜬다. 이때 주소 → 좌표 변환만 실패(400)로 처리된다.

## 측정 (Before/After)
| 도구 | 주소 | 용도 |
|---|---|---|
| Prometheus | http://localhost:9090 | 서버 4개의 수치를 5초마다 수집. `Status > Targets`에서 서버가 `UP`인지 확인 |
| Grafana | http://localhost:3000 (admin / admin) | 수치를 그래프로 확인. Prometheus는 이미 연결되어 있다 |
| k6 | 명령으로 실행 | 부하를 걸어 응답 시간·실패율 측정 |

k6 실행: `docker compose run --rm k6 run /scripts/smoke.js`
스크립트는 `k6/` 폴더에 두고, `smoke.js`를 복사해 자기 API에 맞게 고쳐 쓴다.
k6 결과는 터미널에 요약이 나오고 Prometheus에도 쌓여 Grafana에서 볼 수 있다.

측정 순서: 수정 전 코드로 k6 실행 → 결과·그래프 캡처(Before) → 코드 수정 → 같은 스크립트로 다시 실행 → 캡처(After).
서버는 도커 밖(로컬)에서 실행하고, 도커 안의 도구들이 `host.docker.internal`로 접속한다.

## 폴더 규칙
계층 먼저, 그 안에서 기능 폴더로 나눈다.
`controller/order`, `service/order`, `repository/order`, `dto/order`
엔티티는 서버 모듈이 아니라 `core-domain`에 둔다.

## 규칙 문서
| 문서 | 내용 |
|---|---|
| [docs/api-response.md](docs/api-response.md) | 응답 모양, 에러 코드 만들기·던지기, 상태 코드 고르는 기준 |
| [docs/auth.md](docs/auth.md) | 현재 사용자 받기(`@AuthenticationPrincipal LoginUser`), 개발용 테스트 헤더, 주소 규칙 |
| [docs/internal-events.md](docs/internal-events.md) | 서버 간 주문 이벤트(`OrderEvent`), `/internal/**` 공유 키, 새 이벤트 추가 방법 |

## 브랜치
`main`에는 PR로만 반영한다. 작업 브랜치 이름은 `feature/번호-설명` 또는 `feature/설명` (예: `feature/33-order-create`, `feature/order-create`).

## 커밋 메시지
`종류: 내용` 형식으로 쓴다. Jira 번호는 붙이지 않거나, 붙일 때는 `DUDU-` 없이 번호만 쓴다.
예: `feat: 주문 생성 API`, `feat: 주문 생성 API (#33)`, `fix: 장바구니 수량 검증`
종류: `feat`(기능), `fix`(수정), `refactor`(구조 개선), `docs`(문서), `test`(테스트), `chore`(설정·빌드)
