package com.example.ksl.wsd2025.exception;

import com.example.ksl.wsd2025.api.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

// 모든 컨트롤러에서 발생한 예외를 한곳에서 잡아, 통일된 에러 포맷으로 응답한다.
//   {"status":"error","statusCode":404,"message":"..."}
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ===== 4xx: 클라이언트 잘못 =====

    // 404: 대상이 없음
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(NotFoundException e) {
        return error(HttpStatus.NOT_FOUND, e.getMessage());
    }

    // 400: @Valid 검증 실패 (어느 필드가 틀렸는지 메시지를 모아서 알려 줌)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining(" "));
        return error(HttpStatus.BAD_REQUEST, message);
    }

    // 400: JSON 형식 오류 / 타입이 안 맞는 값 / 숫자가 아닌 id
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception e) {
        return error(HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않습니다.");
    }

    // 아래 두 개는 스프링이 원래 404/405로 처리하던 것인데,
    // 맨 아래의 Exception 처리기가 가로채서 500이 되는 것을 막기 위해 따로 처리한다.
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResource(NoResourceFoundException e) {
        return error(HttpStatus.NOT_FOUND, "존재하지 않는 경로입니다.");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException e) {
        return error(HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않는 요청 메서드입니다.");
    }

    // ===== 5xx: 서버 잘못 =====

    // 503: 일시적으로 서비스 불가
    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnavailable(ServiceUnavailableException e) {
        return error(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
    }

    // 500: 위에서 처리하지 못한 모든 예외. 내부 정보가 새지 않도록 고정 메시지만 응답한다.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception e) {
        e.printStackTrace(); // 자세한 원인은 서버 콘솔에만 남긴다
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "서버 처리 중 오류가 발생했습니다.");
    }

    private ResponseEntity<ApiResponse<Void>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(ApiResponse.error(status.value(), message));
    }
}
