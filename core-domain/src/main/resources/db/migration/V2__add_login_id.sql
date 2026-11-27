-- V2: user·owner 에 로그인 ID 추가 (phone 은 로그인용이 아니라 중복 가입 방지용으로만 사용)
ALTER TABLE `user`
    ADD COLUMN login_id VARCHAR(50) NOT NULL AFTER user_id,
    ADD UNIQUE KEY uk_user_login_id (login_id);

ALTER TABLE owner
    ADD COLUMN login_id VARCHAR(50) NOT NULL AFTER owner_id,
    ADD UNIQUE KEY uk_owner_login_id (login_id);
