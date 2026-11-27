package com.main2.delivery.customer.exception;

import org.springframework.http.HttpStatus;

import com.main2.delivery.common.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter 
@RequiredArgsConstructor 
public enum CartErrorCode implements ErrorCode{

    CART_MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "메뉴를 찾을 수 없습니다."),
    CART_MENU_UNAVAILABLE(HttpStatus.CONFLICT, "지금은 담을 수 없는 메뉴입니다."),
    CART_STORE_CLOSED(HttpStatus.CONFLICT, "지금은 주문할 수 없는 가게입니다."),
    CART_STORE_MISMATCH(HttpStatus.CONFLICT, "장바구니에 다른 가게 메뉴가 있습니다."),
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "장바구니에 없는 메뉴입니다."),
    CART_QUANTITY_EXCEEDED(HttpStatus.BAD_REQUEST, "메뉴당 최대 99개까지 담을 수 있습니다.");  

    private final HttpStatus status;
    private final String message;
}
