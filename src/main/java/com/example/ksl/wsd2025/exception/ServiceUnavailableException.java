package com.example.ksl.wsd2025.exception;

// 서비스가 일시적으로 요청을 처리할 수 없을 때 (503). 예: 점검 중, 외부 시스템 장애
public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String message) {
        super(message);
    }
}
