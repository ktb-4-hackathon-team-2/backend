package com.example.ktb_hktn_team2.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity.status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode, e.getMessage()));
    }

    /**
     * @Valid 검증 실패. 어떤 필드가 왜 틀렸는지까지 내려준다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + defaultMessageOf(fieldError))
                .collect(Collectors.joining(", "));

        return ResponseEntity.status(ErrorCode.INVALID_INPUT.getStatus())
                .body(ErrorResponse.of(ErrorCode.INVALID_INPUT, message));
    }

    /**
     * 본문이 없거나 JSON 형식이 깨진 경우.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException e) {
        return ResponseEntity.status(ErrorCode.INVALID_INPUT.getStatus())
                .body(ErrorResponse.of(ErrorCode.INVALID_INPUT, "요청 본문(JSON)을 읽을 수 없습니다."));
    }

    /**
     * 없는 경로로 요청이 온 경우. 아래 catch-all 로 떨어지면 500 이 되므로 여기서 404 로 잡아준다.
     */
    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(Exception e) {
        return ResponseEntity.status(ErrorCode.NOT_FOUND.getStatus())
                .body(ErrorResponse.of(ErrorCode.NOT_FOUND));
    }

    /**
     * 경로는 맞지만 HTTP 메서드가 다른 경우. (예: POST /api/me)
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity.status(ErrorCode.METHOD_NOT_ALLOWED.getStatus())
                .body(ErrorResponse.of(ErrorCode.METHOD_NOT_ALLOWED, e.getMethod() + " 메서드는 지원하지 않습니다."));
    }

    /**
     * 동시에 같은 이메일로 가입 요청이 들어와 unique 제약에 걸린 경우.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.warn("데이터 무결성 제약 위반", e);
        return ResponseEntity.status(ErrorCode.DUPLICATE_EMAIL.getStatus())
                .body(ErrorResponse.of(ErrorCode.DUPLICATE_EMAIL));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("처리되지 않은 예외", e);
        return ResponseEntity.status(ErrorCode.INTERNAL_ERROR.getStatus())
                .body(ErrorResponse.of(ErrorCode.INTERNAL_ERROR));
    }

    private String defaultMessageOf(FieldError fieldError) {
        return fieldError.getDefaultMessage() == null ? "올바르지 않은 값입니다." : fieldError.getDefaultMessage();
    }
}
