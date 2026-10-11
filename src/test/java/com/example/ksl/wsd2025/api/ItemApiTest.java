package com.example.ksl.wsd2025.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ItemApiTest {

    @Autowired
    MockMvc mvc;

    private ResultActions json(MockHttpServletRequestBuilder request, String body) throws Exception {
        return mvc.perform(request.contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private void assertError(ResultActions result, int code) throws Exception {
        result.andExpect(status().is(code))
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.statusCode").value(code))
                .andExpect(jsonPath("$.message").isString());
    }

    @Test
    void 정상_흐름_8개_API() throws Exception {
        mvc.perform(delete("/api/items")).andExpect(status().isOk());                        // 8) DELETE 전체

        json(post("/api/items"), "{\"name\":\"사과\",\"price\":1000}")                       // 3) POST 생성
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").value(1));

        mvc.perform(post("/api/items/1/copy"))                                               // 4) POST 복제
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("사과 (복사)"));

        mvc.perform(get("/api/items"))                                                       // 1) GET 목록
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
        mvc.perform(get("/api/items").param("keyword", "복사"))
                .andExpect(jsonPath("$.data.length()").value(1));

        mvc.perform(get("/api/items/1"))                                                     // 2) GET 단건
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("사과"));

        json(put("/api/items/1"), "{\"name\":\"배\",\"price\":2000}")                        // 5) PUT 전체 교체
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("배"));

        json(put("/api/items/1/price"), "{\"price\":777}")                                   // 6) PUT 가격
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("배"))
                .andExpect(jsonPath("$.data.price").value(777));

        mvc.perform(delete("/api/items/2"))                                                  // 7) DELETE 한 개
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));

        mvc.perform(delete("/api/items"))                                                    // 8) DELETE 전체
                .andExpect(jsonPath("$.data.deletedCount").value(1));
    }

    @Test
    void 에러_응답은_모두_같은_포맷() throws Exception {
        assertError(mvc.perform(get("/api/items/999")), 404);                                // 없는 상품
        assertError(mvc.perform(delete("/api/items/999")), 404);
        assertError(mvc.perform(post("/api/items/999/copy")), 404);
        assertError(mvc.perform(get("/api/nothing")), 404);                                  // 없는 경로

        assertError(json(post("/api/items"), "{\"name\":\"\",\"price\":-1}"), 400);          // 검증 실패
        assertError(json(post("/api/items"), "{ 깨진 json"), 400);                          // JSON 오류
        assertError(mvc.perform(get("/api/items/abc")), 400);                                // id가 숫자가 아님
        assertError(json(put("/api/items/1"), "{\"name\":\"이름만\"}"), 400);               // PUT은 전체 필요

        assertError(mvc.perform(patch("/api/items/1")), 405);                                // 허용 안 된 메서드

        assertError(mvc.perform(get("/api/demo/unavailable")), 503);                         // 5xx 시연
        assertError(mvc.perform(get("/api/demo/server-error")), 500);
    }

    @Test
    void 서버_오류는_내부_내용을_숨긴다() throws Exception {
        mvc.perform(get("/api/demo/server-error"))
                .andExpect(jsonPath("$.message", not(containsString("의도적으로"))));
    }
}
