package com.example.ksl.wsd2025.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;

// 모든 API가 공통으로 사용하는 응답 틀
//   성공: {"status":"success","data":{...}}
//   실패: {"status":"error","statusCode":404,"message":"..."}
// null인 필드는 JSON에서 빠진다 (성공에는 message가, 실패에는 data가 나가지 않게 함)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final String status;
    private final Integer statusCode;
    private final String message;
    private final T data;

    // 생성자를 private으로 막아서 success() / error()로만 만들 수 있게 한다
    private ApiResponse(String status, Integer statusCode, String message, T data) {
        this.status = status;
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("success", null, null, data);
    }

    public static <T> ApiResponse<T> error(int statusCode, String message) {
        return new ApiResponse<>("error", statusCode, message, null);
    }

    // getter: Jackson이 이 값들을 읽어서 JSON으로 만든다
    public String getStatus() { return status; }
    public Integer getStatusCode() { return statusCode; }
    public String getMessage() { return message; }
    public T getData() { return data; }
}
