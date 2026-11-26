package com.team4.sogogi.global.exception;

import lombok.Getter;

/**
 * 서비스 로직에서 던지는 예외.
 * 예: throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "id=" + id);
 *     → message: "존재하지 않는 상품입니다. id=99"
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String detail) {
        super(errorCode.getMessage() + " " + detail);
        this.errorCode = errorCode;
    }
}
