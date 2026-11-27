-- V3: 주문 생성(DUDU-33)·취소(DUDU-30)에 필요한 컬럼 추가
--   order_key   : 중복 주문 방지 (앱이 주문서 진입 시 만든 값, 같은 값이면 기존 주문 반환)
--   delivery_*  : 주문 시점 배달지 상세 주소·좌표 복사 (주소를 바꾸거나 지워도 주문은 그대로)
--   payment_method : 결제 수단 선택값 (Sprint 1은 저장만)
--   version     : 낙관적 락 (점주 접수 vs 고객 취소 동시 요청)
ALTER TABLE orders
    ADD COLUMN order_key               VARCHAR(64)   NOT NULL                AFTER order_id,
    ADD COLUMN delivery_address_detail VARCHAR(100)  NULL                    AFTER delivery_address,
    ADD COLUMN delivery_lat            DECIMAL(10,7) NOT NULL                AFTER delivery_address_detail,
    ADD COLUMN delivery_lng            DECIMAL(10,7) NOT NULL                AFTER delivery_lat,
    ADD COLUMN payment_method          VARCHAR(20)   NOT NULL DEFAULT 'CARD' AFTER request_note,
    ADD COLUMN version                 BIGINT        NOT NULL DEFAULT 0      AFTER payment_method,
    ADD UNIQUE KEY uk_orders_order_key (order_key);