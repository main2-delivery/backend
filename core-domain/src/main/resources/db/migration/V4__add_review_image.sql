-- V4: 리뷰 사진 테이블 추가 (리뷰 1개에 사진 여러 장)
--   review_id  : 어느 리뷰의 사진인지 (review.review_id 참조)
--   image_url  : 사진 주소 (menu.image_url과 같은 방식, 파일은 외부 저장소에 두고 주소만 저장)
--   sort_order : 사진 순서 (0부터, 첫 번째 사진이 대표)
--   review 테이블은 바꾸지 않음. 기존 수집 리뷰는 사진 0장
CREATE TABLE review_image (
    review_image_id BIGINT        NOT NULL AUTO_INCREMENT,
    review_id       BIGINT        NOT NULL,
    image_url       VARCHAR(1000) NOT NULL,
    sort_order      INT           NOT NULL DEFAULT 0,
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (review_image_id),
    KEY idx_review_image_review (review_id, sort_order),
    CONSTRAINT fk_review_image_review FOREIGN KEY (review_id) REFERENCES review (review_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;