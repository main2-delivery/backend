package com.main2.delivery.owner.dto.menu;

import com.main2.delivery.core.entity.MenuEntity;

import lombok.Getter;

// 메뉴 조회 응답 (필드 이름은 DUDU-9 메뉴판 형식과 맞출 것)
@Getter
public class MenuResponse {

    private Long menuId;
    private String name;
    private String description;
    private Integer price;          // 수집 메뉴는 null일 수 있음
    private String imageUrl;
    private boolean soldOut;
    private boolean hidden;
    private boolean representative;

    public MenuResponse(MenuEntity entity) {
        this.menuId = entity.getId();
        this.name = entity.getMenuName();
        this.description = entity.getDescription();
        this.price = entity.getPrice();
        this.imageUrl = entity.getImageUrl();
        this.soldOut = entity.isSoldOut();
        this.hidden = entity.isHidden();
        this.representative = entity.isRepresentative();
    }
}