# 디쉬패치 백엔드

위치 기반 배달 주문·배차 플랫폼. Java 21, Spring Boot 4.1.3, Gradle 멀티 모듈.

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
1. `docker compose up -d` (MySQL, Redis)
2. `./gradlew :customer-server:bootRun`
3. `./gradlew :owner-server:bootRun`

접속 정보 기본값은 `docker-compose.yml`과 맞춰져 있어 `.env` 없이도 로컬에서 실행된다.
값을 바꾸려면 `.env.example`을 `.env`로 복사해 수정한다. `.env`는 올리지 않는다.

## 폴더 규칙
계층 먼저, 그 안에서 기능 폴더로 나눈다.
`controller/order`, `service/order`, `repository/order`, `dto/order`
엔티티는 서버 모듈이 아니라 `core-domain`에 둔다.

## 로그인 없이 테스트하기
요청 헤더에 `X-Test-User-Id`, `X-Test-Role`을 넣는다.

## 서버 간 호출 (Sprint 1)
`/internal/**` 경로, REST, 공유 키 헤더로 보호. Sprint 2에 RabbitMQ로 전환.

## 브랜치
`main`에는 PR로만 반영한다. 작업 브랜치 이름은 `feature/번호-설명` 또는 `feature/설명` (예: `feature/33-order-create`, `feature/order-create`).

## 커밋 메시지
`종류: 내용` 형식으로 쓴다. Jira 번호는 붙이지 않거나, 붙일 때는 `DUDU-` 없이 번호만 쓴다.
예: `feat: 주문 생성 API`, `feat: 주문 생성 API (#33)`, `fix: 장바구니 수량 검증`
종류: `feat`(기능), `fix`(수정), `refactor`(구조 개선), `docs`(문서), `test`(테스트), `chore`(설정·빌드)
