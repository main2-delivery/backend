-- V5: 고객 닉네임 중복 금지 (리뷰 표시명). 컬럼은 NULL 허용 그대로, UNIQUE는 NULL 여러 개를 허용
ALTER TABLE `user`
    ADD UNIQUE KEY uk_user_nickname (nickname);