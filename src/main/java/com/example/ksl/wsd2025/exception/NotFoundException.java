package com.example.ksl.wsd2025.exception;

// 대상이 없을 때 (404)
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
