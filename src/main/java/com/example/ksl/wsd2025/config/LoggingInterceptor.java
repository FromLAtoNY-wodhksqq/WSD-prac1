package com.example.ksl.wsd2025.config;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString();

        // 처리 시간을 재기 위해 시작 시각을 요청에 저장해 둔다
        request.setAttribute("startTime", System.currentTimeMillis());

        System.out.println("[LoggingInterceptor] 요청 시작: " + method + " " + uri +
                (query != null ? "?" + query : ""));
        return true; // false로 하면 컨트롤러로 안 넘어감
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex
    ) {
        long startTime = (Long) request.getAttribute("startTime");
        long elapsed = System.currentTimeMillis() - startTime;

        System.out.println("[LoggingInterceptor] 요청 완료: " + request.getMethod() + " " + request.getRequestURI() +
                " status=" + response.getStatus() + " (" + elapsed + "ms)");
    }
}