package com.main2.delivery.customer.dto.store;

import com.main2.delivery.core.entity.MenuEntity;

import lombok.Getter;

@Getter
public class MenuResponse {

    private Long menuId;
    private String menuName;
    private String description;
    private Integer price;
    private String imageUrl;
    private boolean soldOut;
    private boolean representative;

    public MenuResponse(MenuEntity menu) {
        this.menuId = menu.getId();
        this.menuName = menu.getMenuName();
        this.description = menu.getDescription();
        this.price = menu.getPrice();
        this.imageUrl = menu.getImageUrl();
        this.soldOut = menu.isSoldOut();
        this.representative = menu.isRepresentative();
    }
}