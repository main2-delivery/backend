-- V1: 입점 신청(DUDU-17)용 사업자등록번호(형식만 검사)·배달 반경(미터) 컬럼 추가
ALTER TABLE store
    ADD COLUMN business_number   VARCHAR(12) NULL                AFTER store_phone,
    ADD COLUMN delivery_radius_m INT         NOT NULL DEFAULT 3000 AFTER delivery_fee,
    ADD CONSTRAINT ck_store_delivery_radius CHECK (delivery_radius_m > 0);
