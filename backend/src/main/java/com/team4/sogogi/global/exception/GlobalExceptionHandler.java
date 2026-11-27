package com.team4.sogogi.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * 컨트롤러에서 발생한 예외를 ApiErrorResponse 형식으로 일괄 변환한다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 404, 409 등 서비스 로직에서 던진 예외 */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(BusinessException e) {
        return toResponse(ApiErrorResponse.of(e.getErrorCode(), e.getMessage()));
    }

    /** 400: @Valid @RequestBody 검증 실패 (이메일 형식, 수량 0 이하, 가격 음수 등) */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(GlobalExceptionHandler::formatFieldError)
                .collect(Collectors.joining(", "));
        return toResponse(ApiErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, message));
    }

    /** 400: @PathVariable·@RequestParam 등 메서드 파라미터 제약 검증 실패 */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleHandlerMethodValidation(HandlerMethodValidationException e) {
        String message = e.getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return toResponse(ApiErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, message));
    }

    /** 400: 타입 불일치 (예: /products/abc) */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String message = e.getName() + ": 올바르지 않은 값입니다. (" + e.getValue() + ")";
        return toResponse(ApiErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, message));
    }

    /** 400: JSON 본문 형식 오류 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleNotReadable(HttpMessageNotReadableException e) {
        return toResponse(ApiErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, "요청 본문 형식이 올바르지 않습니다."));
    }

    /** 400: 필수 쿼리 파라미터 누락 (예: GET /api/orders 에 ?email= 없음) */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingParameter(MissingServletRequestParameterException e) {
        String message = e.getParameterName() + ": 필수 파라미터입니다.";
        return toResponse(ApiErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, message));
    }

    /** 404: 매핑된 API가 없는 주소 */
    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public ResponseEntity<ApiErrorResponse> handleNoResource(Exception e) {
        return toResponse(ApiErrorResponse.of(ErrorCode.RESOURCE_NOT_FOUND));
    }

    /** 405: 주소는 있지만 HTTP 메서드가 다름 (응답에 허용 메서드를 Allow 헤더로 알려 준다) */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        ApiErrorResponse body = ApiErrorResponse.of(ErrorCode.METHOD_NOT_ALLOWED);
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(body.status());
        if (e.getSupportedHttpMethods() != null) {
            builder.allow(e.getSupportedHttpMethods().toArray(HttpMethod[]::new));
        }
        return builder.body(body);
    }

    /** 415: JSON이 아닌 Content-Type으로 본문을 보냄 */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        return toResponse(ApiErrorResponse.of(ErrorCode.UNSUPPORTED_MEDIA_TYPE));
    }

    /** 500: 예상하지 못한 예외 (내부 메시지는 응답에 노출하지 않는다) */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(Exception e) {
        log.error("Unhandled exception", e);
        return toResponse(ApiErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR));
    }

    private static String formatFieldError(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }

    private static ResponseEntity<ApiErrorResponse> toResponse(ApiErrorResponse body) {
        return ResponseEntity.status(body.status()).body(body);
    }
}
