package com.main2.delivery.customer.exception;

import org.springframework.http.HttpStatus;

import com.main2.delivery.common.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {

    ORDER_CART_EMPTY(HttpStatus.BAD_REQUEST, "장바구니가 비어 있습니다."),
    ORDER_STORE_CLOSED(HttpStatus.BAD_REQUEST, "지금은 주문할 수 없는 가게입니다."),
    ORDER_MENU_UNAVAILABLE(HttpStatus.BAD_REQUEST, "주문할 수 없는 메뉴가 있습니다. 장바구니를 확인해주세요."),
    ORDER_MIN_AMOUNT_NOT_MET(HttpStatus.BAD_REQUEST, "최소 주문 금액을 채워주세요."),
    ORDER_OUT_OF_DELIVERY_AREA(HttpStatus.BAD_REQUEST, "배달 가능한 거리를 벗어난 주소입니다."),
    ORDER_ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "배달 주소를 찾을 수 없습니다."),
    ORDER_KEY_CONFLICT(HttpStatus.CONFLICT, "이미 사용된 주문 키입니다.");

    private final HttpStatus status;
    private final String message;
}