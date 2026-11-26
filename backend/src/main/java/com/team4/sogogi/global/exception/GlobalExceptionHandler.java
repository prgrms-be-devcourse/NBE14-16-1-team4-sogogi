package com.team4.sogogi.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

/**
 * 컨트롤러에서 발생한 예외를 ErrorResponse 형식으로 일괄 변환한다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 404, 409 등 서비스 로직에서 던진 예외 */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        return toResponse(ErrorResponse.of(e.getErrorCode(), e.getMessage()));
    }

    /** 400: @Valid @RequestBody 검증 실패 (이메일 형식, 수량 0 이하, 가격 음수 등) */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(GlobalExceptionHandler::formatFieldError)
                .collect(Collectors.joining(", "));
        return toResponse(ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, message));
    }

    /** 400: @PathVariable·@RequestParam 등 메서드 파라미터 제약 검증 실패 */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidation(HandlerMethodValidationException e) {
        String message = e.getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return toResponse(ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, message));
    }

    /** 400: 타입 불일치 (예: /products/abc) */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String message = e.getName() + ": 올바르지 않은 값입니다. (" + e.getValue() + ")";
        return toResponse(ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, message));
    }

    /** 400: JSON 본문 형식 오류 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException e) {
        return toResponse(ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, "요청 본문 형식이 올바르지 않습니다."));
    }

    /** 500: 예상하지 못한 예외 (내부 메시지는 응답에 노출하지 않는다) */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unhandled exception", e);
        return toResponse(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR));
    }

    private static String formatFieldError(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }

    private static ResponseEntity<ErrorResponse> toResponse(ErrorResponse body) {
        return ResponseEntity.status(body.status()).body(body);
    }
}
