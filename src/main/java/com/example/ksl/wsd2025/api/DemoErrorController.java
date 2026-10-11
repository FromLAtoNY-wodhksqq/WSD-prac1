package com.example.ksl.wsd2025.api;

import com.example.ksl.wsd2025.exception.ServiceUnavailableException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 5xx 응답을 확인하기 위한 시연용 API (과제의 8개 API에는 포함되지 않음)
@RestController
@RequestMapping("/api/demo")
public class DemoErrorController {

    // 500: 예상하지 못한 서버 내부 오류를 일부러 발생시킨다
    @GetMapping("/server-error")
    public void serverError() {
        throw new IllegalStateException("의도적으로 발생시킨 내부 오류");
    }

    // 503: 서비스 일시 중단 상황을 흉내 낸다
    @GetMapping("/unavailable")
    public void unavailable() {
        throw new ServiceUnavailableException("서비스 점검 중입니다. 잠시 후 다시 시도해 주세요.");
    }
}
