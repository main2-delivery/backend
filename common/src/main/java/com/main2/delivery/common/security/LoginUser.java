package com.main2.delivery.common.security;

import com.main2.delivery.common.type.Role;

// 컨트롤러에서 @AuthenticationPrincipal LoginUser 로 받는 현재 사용자
// id는 role에 따라 user_id 또는 owner_id (두 테이블 id가 겹치므로 항상 role과 함께 볼 것)
public record LoginUser(Long id, Role role) {

    public boolean isCustomer() {
        return role == Role.CUSTOMER;
    }

    public boolean isOwner(){
        return role == Role.OWNER;
    }
}
