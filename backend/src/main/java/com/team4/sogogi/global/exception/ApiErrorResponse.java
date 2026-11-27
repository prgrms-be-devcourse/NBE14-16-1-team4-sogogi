package com.team4.sogogi.global.exception;

/**
 * 에러 응답 본문.
 * { "status": 404, "code": "PRODUCT_NOT_FOUND", "message": "존재하지 않는 상품입니다. id=99" }
 */
public record ErrorResponse(int status, String code, String message) {

    public static ErrorResponse of(ErrorCode errorCode) {
        return of(errorCode, errorCode.getMessage());
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(errorCode.getStatus().value(), errorCode.name(), message);
    }
}
