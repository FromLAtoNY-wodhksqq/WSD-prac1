# WSD-prac1 : Spring Boot 상품(Item) REST API

Spring Boot로 구현한 상품 관리 REST API 서버입니다.
HTTP 메서드별 API 구현, 미들웨어, 다양한 응답 코드, 통일된 응답 포맷을 연습하는 과제입니다.

## 실행 방법

- 필요 환경: **JDK 21**
- 실행

```bash
./gradlew bootRun
```

서버가 `http://localhost:8080` 에서 실행됩니다. 데이터는 서버 메모리에 저장되므로 서버를 끄면 사라집니다.

테스트 실행

```bash
./gradlew test
```

## API 목록 (8개)

기본 경로: `/api/items`

| # | Method | URL | 설명 | 성공 | 실패 |
|---|--------|-----|------|------|------|
| 1 | GET | `/api/items?keyword=` | 상품 목록 조회 (keyword로 이름 검색) | 200 | |
| 2 | GET | `/api/items/{id}` | 상품 단건 조회 | 200 | 404 |
| 3 | POST | `/api/items` | 상품 등록 | 201 | 400 |
| 4 | POST | `/api/items/{id}/copy` | 상품 복제 | 201 | 404 |
| 5 | PUT | `/api/items/{id}` | 상품 전체 수정 (name, price 모두 필요) | 200 | 400, 404 |
| 6 | PUT | `/api/items/{id}/price` | 가격만 수정 | 200 | 400, 404 |
| 7 | DELETE | `/api/items/{id}` | 상품 삭제 | 200 | 404 |
| 8 | DELETE | `/api/items` | 상품 전체 삭제 | 200 | |

5xx 응답을 확인하기 위한 시연용 API (위 8개에는 포함되지 않음)

| Method | URL | 응답 |
|--------|-----|------|
| GET | `/api/demo/server-error` | 500 |
| GET | `/api/demo/unavailable` | 503 |

## 응답 포맷

성공과 실패 모두 같은 틀을 사용합니다.

성공

```json
{
  "status": "success",
  "data": { "id": 1, "name": "사과", "price": 1000 }
}
```

실패

```json
{
  "status": "error",
  "statusCode": 404,
  "message": "상품을 찾을 수 없습니다. id=999"
}
```

삭제(7번)는 `data` 없이 `{"status": "success"}` 로 응답합니다.

## 사용한 응답 코드

| 구분 | 코드 | 사용하는 경우 |
|------|------|---------------|
| 2xx | 200 OK | 조회, 수정, 삭제 성공 |
| 2xx | 201 Created | 등록, 복제 성공 |
| 4xx | 400 Bad Request | 입력값 검증 실패, JSON 형식 오류, id가 숫자가 아님 |
| 4xx | 404 Not Found | 없는 상품, 없는 경로 |
| 4xx | 405 Method Not Allowed | 허용되지 않는 메서드 |
| 5xx | 500 Internal Server Error | 예상하지 못한 서버 오류 (내부 내용은 숨김) |
| 5xx | 503 Service Unavailable | 서비스 일시 중단 (시연용) |

## 미들웨어

`LoggingInterceptor` 가 `/api/**` 요청마다 시작과 완료를 콘솔에 기록합니다. 처리 시간(ms)도 함께 출력합니다.

```
[LoggingInterceptor] 요청 시작: POST /api/items
[LoggingInterceptor] 요청 완료: POST /api/items status=201 (2ms)
```

## 프로젝트 구조

```
src/main/java/com/example/ksl/wsd2025
├── api
│   ├── ItemApiController.java     # 상품 API 8개
│   ├── DemoErrorController.java   # 5xx 시연용 API
│   ├── dto/ItemDto.java           # 응답에 쓰는 상품 데이터
│   ├── request/                   # 요청 바디 + 입력 검증 규칙
│   └── response/ApiResponse.java  # 공통 응답 포맷
├── config
│   ├── LoggingInterceptor.java    # 미들웨어 (요청 로그)
│   └── WebConfig.java             # 미들웨어 등록
└── exception
    ├── GlobalExceptionHandler.java  # 예외를 통일된 에러 포맷으로 변환
    ├── NotFoundException.java
    └── ServiceUnavailableException.java
```

## 동작 흐름

1. 요청이 들어오면 `LoggingInterceptor` 가 로그를 남깁니다.
2. 요청 바디를 객체로 변환하고 `@Valid` 로 입력값을 검증합니다.
3. `ItemApiController` 가 처리하고 `ApiResponse` 로 감싸 응답합니다.
4. 중간에 예외가 발생하면 `GlobalExceptionHandler` 가 상태 코드와 에러 포맷으로 변환합니다.

## 테스트 방법 (curl 예시)

```bash
# 등록 (201)
curl -X POST http://localhost:8080/api/items \
  -H "Content-Type: application/json" \
  -d '{"name":"사과","price":1000}'

# 조회 (200)
curl http://localhost:8080/api/items/1

# 없는 상품 (404)
curl http://localhost:8080/api/items/999

# 입력 오류 (400)
curl -X POST http://localhost:8080/api/items \
  -H "Content-Type: application/json" \
  -d '{"name":"","price":-1}'

# 서버 오류 (500), 서비스 불가 (503)
curl http://localhost:8080/api/demo/server-error
curl http://localhost:8080/api/demo/unavailable
```

한글 검색어는 URL 인코딩이 필요합니다: `curl -G http://localhost:8080/api/items --data-urlencode 'keyword=사과'`

IntelliJ를 사용한다면 저장소의 `requests.http` 파일에서 모든 요청을 바로 실행할 수 있습니다.

## 참고

- 삭제 응답은 공통 응답 포맷을 유지하기 위해 204 대신 200과 본문으로 응답합니다.
- 데이터는 `HashMap` 에 저장하는 임시 방식입니다.
