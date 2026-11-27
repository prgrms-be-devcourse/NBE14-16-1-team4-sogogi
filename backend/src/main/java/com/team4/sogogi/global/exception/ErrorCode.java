package com.team4.sogogi.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 에러 응답의 status / code / message 정의.
 * 새 예외 상황은 여기에 상수를 추가한다.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 400 Bad Request: 입력값 검증 실패
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),

    // 404 Not Found: 존재하지 않는 리소스
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 상품입니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 주문입니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 주소를 찾을 수 없습니다."),

    // 405 Method Not Allowed: 지원하지 않는 HTTP 메서드
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다."),

    // 415 Unsupported Media Type: 지원하지 않는 Content-Type
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 Content-Type입니다."),

    // 409 Conflict: 상태 충돌
    ORDER_ALREADY_SHIPPED(HttpStatus.CONFLICT, "이미 배송된 주문은 수정·취소할 수 없습니다."),
    PRODUCT_HAS_ORDERS(HttpStatus.CONFLICT, "주문 이력이 있는 상품은 삭제할 수 없습니다."),

    // 500 Internal Server Error: 예상하지 못한 예외
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;
}
