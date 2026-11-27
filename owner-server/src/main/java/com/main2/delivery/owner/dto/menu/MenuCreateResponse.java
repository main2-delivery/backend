package com.main2.delivery.owner.dto.menu;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 메뉴 등록 응답 { "menuId": 101 }
@Getter
@AllArgsConstructor
public class MenuCreateResponse {
    private Long menuId;
}