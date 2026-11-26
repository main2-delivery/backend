-- V0: 초기 스키마 (MVP)
-- 기준: ERD 탭 최종본(테이블 10개) + db_import CSV(store / menu / review)
-- 적용된 뒤에는 이 파일을 고치지 않는다. 컬럼·테이블 추가는 V1, V2 ... 새 파일로 쌓는다.
--
-- CSV → 테이블 (load_db.py 가 넣음)
--   store.csv  → store  (store_id, store_name, store_phone, address, lat, lng, image_url)
--   menu.csv   → menu   (store_id, menu_name, price, image_url)
--   review.csv → review (store_id, rating, body)   ※ CSV 의 visited 는 테이블에 없어 건너뜀
--   CSV 에 없는 컬럼은 모두 NULL 허용이거나 기본값이 있다.
--
-- 상태 값은 ENUM 대신 VARCHAR 로 저장한다 (엔티티는 @Enumerated(EnumType.STRING)).
-- 장바구니·리프레시 토큰은 Redis 에 두므로 테이블이 없다.


-- =====================================================================
-- 회원: 개인 회원(user) 과 업주 회원(owner) 은 별도 테이블
-- 같은 휴대전화 번호로 개인 회원·업주 회원을 각각 가질 수 있다
-- =====================================================================

-- 개인 회원 (고객 앱). 로그인: POST /api/customer/auth/login
CREATE TABLE `user` (
    user_id       BIGINT       NOT NULL AUTO_INCREMENT,
    phone         VARCHAR(20)  NOT NULL,                     -- 로그인 ID. 숫자만 (01012345678)
    password_hash VARCHAR(255) NOT NULL,                     -- BCrypt 등으로 암호화
    name          VARCHAR(50)  NOT NULL,
    nickname      VARCHAR(50)  NULL,                         -- 리뷰 표시명
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',    -- ACTIVE / WITHDRAWN
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_user_phone (phone)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 업주 회원 (업주 웹). 로그인: POST /api/owner/auth/login
CREATE TABLE owner (
    owner_id      BIGINT       NOT NULL AUTO_INCREMENT,
    phone         VARCHAR(20)  NOT NULL,                     -- 로그인 ID. 숫자만
    password_hash VARCHAR(255) NOT NULL,
    name          VARCHAR(50)  NOT NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',    -- ACTIVE / WITHDRAWN
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (owner_id),
    UNIQUE KEY uk_owner_phone (phone)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 배달 주소. 회원당 기본 배송지(is_default = 1)는 1개 (애플리케이션에서 보장)
CREATE TABLE user_address (
    address_id     BIGINT        NOT NULL AUTO_INCREMENT,
    user_id        BIGINT        NOT NULL,
    label          VARCHAR(20)   NULL,                       -- 집, 회사
    address        VARCHAR(255)  NOT NULL,
    address_detail VARCHAR(100)  NULL,
    lat            DECIMAL(10,7) NOT NULL,
    lng            DECIMAL(10,7) NOT NULL,
    is_default     BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (address_id),
    CONSTRAINT fk_address_user FOREIGN KEY (user_id) REFERENCES `user` (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;


-- =====================================================================
-- 가게 · 메뉴
-- =====================================================================

-- 고객 앱 홈 카테고리 탭 (치킨, 분식 ...). 데이터는 나중에 V 파일로 추가
CREATE TABLE store_category (
    category_id   BIGINT      NOT NULL AUTO_INCREMENT,
    category_name VARCHAR(50) NOT NULL,
    sort_order    INT         NOT NULL DEFAULT 0,
    created_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (category_id),
    UNIQUE KEY uk_store_category_name (category_name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- store_id: 수집 가게는 네이버 place_id 를 그대로 넣는다 (owner_id NULL).
-- 입점 가게는 AUTO_INCREMENT 가 (가장 큰 place_id + 1)부터 자동 발급한다.
CREATE TABLE store (
    store_id         BIGINT        NOT NULL AUTO_INCREMENT,
    owner_id         BIGINT        NULL,
    category_id      BIGINT        NULL,
    store_name       VARCHAR(100)  NOT NULL,
    store_phone      VARCHAR(20)   NULL,                     -- 하이픈 포함 (02-747-1611)
    address          VARCHAR(255)  NOT NULL,
    lat              DECIMAL(10,7) NOT NULL,
    lng              DECIMAL(10,7) NOT NULL,
    min_order_amount INT           NOT NULL DEFAULT 0,
    delivery_fee     INT           NOT NULL DEFAULT 0,
    open_time        TIME          NULL,
    close_time       TIME          NULL,
    status           VARCHAR(20)   NOT NULL DEFAULT 'OPEN',  -- OPEN / PREPARING / PAUSED / CLOSED
    image_url        VARCHAR(1000) NULL,
    created_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (store_id),
    KEY idx_store_lat_lng (lat, lng),                        -- 주변 가게 조회 (반경 계산 전 범위 필터)
    CONSTRAINT fk_store_owner    FOREIGN KEY (owner_id)    REFERENCES owner (owner_id),
    CONSTRAINT fk_store_category FOREIGN KEY (category_id) REFERENCES store_category (category_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 주문 이력이 있는 메뉴도 지울 수 있게 is_deleted 로 표시만 한다
CREATE TABLE menu (
    menu_id           BIGINT        NOT NULL AUTO_INCREMENT,
    store_id          BIGINT        NOT NULL,
    menu_name         VARCHAR(100)  NOT NULL,
    description       VARCHAR(500)  NULL,
    price             INT           NULL,                    -- NULL = 가격 미표기 (주문 불가)
    image_url         VARCHAR(1000) NULL,
    is_sold_out       BOOLEAN       NOT NULL DEFAULT FALSE,
    is_hidden         BOOLEAN       NOT NULL DEFAULT FALSE,
    is_representative BOOLEAN       NOT NULL DEFAULT FALSE,
    is_deleted        BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (menu_id),
    CONSTRAINT fk_menu_store FOREIGN KEY (store_id) REFERENCES store (store_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;


-- =====================================================================
-- 주문
-- 상태: PENDING → ACCEPTED → COOKING → COOKED → DELIVERING → DELIVERED
--       끝 상태: REJECTED / CANCELED
-- =====================================================================

CREATE TABLE orders (
    order_id         BIGINT       NOT NULL AUTO_INCREMENT,
    store_id         BIGINT       NOT NULL,
    user_id          BIGINT       NOT NULL,
    status           VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    menu_amount      INT          NOT NULL,                  -- 단가 × 수량 합계
    delivery_fee     INT          NOT NULL DEFAULT 0,
    total_price      INT          NOT NULL,                  -- 메뉴 합계 + 배달비
    delivery_address VARCHAR(255) NOT NULL,                  -- 주문 시점 주소 복사
    request_note     VARCHAR(255) NULL,
    cancel_reason    VARCHAR(255) NULL,                      -- 거절·취소 사유
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,   -- 주문 시각
    updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    accepted_at      DATETIME     NULL,
    cooked_at        DATETIME     NULL,
    completed_at     DATETIME     NULL,                      -- 배달 완료
    canceled_at      DATETIME     NULL,
    PRIMARY KEY (order_id),
    KEY idx_orders_store_status (store_id, status, created_at),  -- 업주 주문 목록
    KEY idx_orders_user (user_id, created_at),                    -- 고객 주문 내역
    CONSTRAINT fk_orders_store FOREIGN KEY (store_id) REFERENCES store (store_id),
    CONSTRAINT fk_orders_user  FOREIGN KEY (user_id)  REFERENCES `user` (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 메뉴명·단가는 주문 시점 값을 복사 (메뉴가 바뀌거나 지워져도 주문 내역 유지)
CREATE TABLE order_item (
    order_item_id BIGINT       NOT NULL AUTO_INCREMENT,
    order_id      BIGINT       NOT NULL,
    menu_id       BIGINT       NULL,
    menu_name     VARCHAR(100) NOT NULL,
    unit_price    INT          NOT NULL,
    quantity      INT          NOT NULL DEFAULT 1,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (order_item_id),
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES orders (order_id),
    CONSTRAINT fk_order_item_menu  FOREIGN KEY (menu_id)  REFERENCES menu (menu_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 결제: 지금은 PK·FK 만. 금액·상태·PG 컬럼은 결제 연동 때 새 V 파일에서 ALTER TABLE 로 추가
CREATE TABLE payment (
    payment_id BIGINT NOT NULL AUTO_INCREMENT,
    order_id   BIGINT NOT NULL,
    PRIMARY KEY (payment_id),
    UNIQUE KEY uk_payment_order (order_id),                  -- 주문당 결제 1건
    CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES orders (order_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;


-- =====================================================================
-- 리뷰
-- 수집 리뷰는 user_id·order_id 가 NULL. 앱 리뷰는 배달 완료 주문당 1개
-- =====================================================================

CREATE TABLE review (
    review_id  BIGINT       NOT NULL AUTO_INCREMENT,
    store_id   BIGINT       NOT NULL,
    user_id    BIGINT       NULL,
    order_id   BIGINT       NULL,
    rating     DECIMAL(2,1) NULL,                            -- 0.5 단위 (0.5 ~ 5.0)
    body       TEXT         NULL,
    reply      TEXT         NULL,                            -- 점주 답글
    replied_at DATETIME     NULL,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (review_id),
    UNIQUE KEY uk_review_order (order_id),                   -- NULL 은 여러 개 허용
    KEY idx_review_store (store_id, created_at),
    CONSTRAINT fk_review_store FOREIGN KEY (store_id) REFERENCES store (store_id),
    CONSTRAINT fk_review_user  FOREIGN KEY (user_id)  REFERENCES `user` (user_id),
    CONSTRAINT fk_review_order FOREIGN KEY (order_id) REFERENCES orders (order_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;