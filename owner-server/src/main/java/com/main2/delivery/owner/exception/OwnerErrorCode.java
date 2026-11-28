package com.main2.delivery.owner.exception;

import com.main2.delivery.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

// 업주 서버 에러 코드 (공통 코드는 common 의 CommonErrorCode 사용)
// 메시지는 화면에 그대로 보여 줄 문장: 존댓말 + 마침표 (docs/api-response.md 3장)
@Getter
@RequiredArgsConstructor
public enum OwnerErrorCode implements ErrorCode {

    OWNER_ROLE_REQUIRED(HttpStatus.FORBIDDEN, "업주 계정만 사용할 수 있습니다."),
    OWNER_NOT_FOUND(HttpStatus.NOT_FOUND, "업주 계정을 찾을 수 없습니다."),
    STORE_INVALID_NAME(HttpStatus.BAD_REQUEST, "가게 이름은 1~100자로 입력해 주세요."),
    STORE_INVALID_ADDRESS(HttpStatus.BAD_REQUEST, "주소는 1~255자로 입력해 주세요."),
    STORE_INVALID_PHONE(HttpStatus.BAD_REQUEST, "전화번호는 02-123-4567 형식으로 입력해 주세요."),
    STORE_INVALID_BUSINESS_NUMBER(HttpStatus.BAD_REQUEST, "사업자등록번호는 000-00-00000 형식입니다."),
    STORE_INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "최소주문금액과 배달팁은 0원 이상이어야 합니다."),
    STORE_INVALID_DELIVERY_RADIUS(HttpStatus.BAD_REQUEST, "배달 반경은 1m 이상이어야 합니다."),
    STORE_INVALID_LOGO_URL(HttpStatus.BAD_REQUEST, "이미지 주소는 1000자까지 입력할 수 있습니다."),
    STORE_INVALID_BUSINESS_HOURS(HttpStatus.BAD_REQUEST, "여는 시간과 닫는 시간은 함께 입력해야 하고, 서로 달라야 합니다."),
    STORE_GEOCODING_FAILED(HttpStatus.BAD_REQUEST, "주소를 좌표로 바꿀 수 없습니다. 도로명 주소를 확인해 주세요."),
    STORE_CATEGORY_NOT_FOUND(HttpStatus.BAD_REQUEST, "없는 가게 카테고리입니다."),
    STORE_NOT_OWNER(HttpStatus.FORBIDDEN, "내 가게가 아닙니다."),
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "가게를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;
}
