package com.example.ksl.wsd2025.api;

import com.example.ksl.wsd2025.api.dto.ItemDto;
import com.example.ksl.wsd2025.api.request.ItemCreateRequest;
import com.example.ksl.wsd2025.api.request.ItemUpdateRequest;
import com.example.ksl.wsd2025.api.request.PriceUpdateRequest;
import com.example.ksl.wsd2025.api.response.ApiResponse;
import com.example.ksl.wsd2025.exception.NotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/items")
public class ItemApiController {

    // 메모리용 임시 DB (예제용)
    private final Map<Long, ItemDto> store = new HashMap<>();
    private long sequence = 1L;

    // ===== GET 2개 =====

    // 1) GET: 목록 조회 (keyword가 있으면 이름에 포함된 것만)  → 200
    @GetMapping
    public ResponseEntity<ApiResponse<List<ItemDto>>> getItems(
            @RequestParam(required = false) String keyword
    ) {
        List<ItemDto> items = new ArrayList<>();
        for (ItemDto item : store.values()) {
            if (keyword == null || item.getName().contains(keyword)) {
                items.add(item);
            }
        }
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    // 2) GET: 단건 조회  → 200 / 없으면 404
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemDto>> getItem(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(findOrThrow(id)));
    }

    // ===== POST 2개 =====

    // 3) POST: 생성  → 201 / 입력 오류 400
    @PostMapping
    public ResponseEntity<ApiResponse<ItemDto>> createItem(
            @Valid @RequestBody ItemCreateRequest request
    ) {
        ItemDto item = save(request.getName(), request.getPrice());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(item));
    }

    // 4) POST: 복제 (기존 상품과 같은 가격의 새 상품 생성)  → 201 / 없으면 404
    @PostMapping("/{id}/copy")
    public ResponseEntity<ApiResponse<ItemDto>> copyItem(@PathVariable Long id) {
        ItemDto origin = findOrThrow(id);
        ItemDto copy = save(origin.getName() + " (복사)", origin.getPrice());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(copy));
    }

    // ===== PUT 2개 =====

    // 5) PUT: 전체 교체 (name, price 모두 필요)  → 200 / 400 / 404
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemDto>> replaceItem(
            @PathVariable Long id,
            @Valid @RequestBody ItemUpdateRequest request
    ) {
        ItemDto item = findOrThrow(id);
        item.setName(request.getName());
        item.setPrice(request.getPrice());
        return ResponseEntity.ok(ApiResponse.success(item));
    }

    // 6) PUT: 가격만 교체  → 200 / 400 / 404
    @PutMapping("/{id}/price")
    public ResponseEntity<ApiResponse<ItemDto>> updatePrice(
            @PathVariable Long id,
            @Valid @RequestBody PriceUpdateRequest request
    ) {
        ItemDto item = findOrThrow(id);
        item.setPrice(request.getPrice());
        return ResponseEntity.ok(ApiResponse.success(item));
    }

    // ===== DELETE 2개 =====
    // 204는 응답 본문이 없어야 해서 공통 포맷을 쓸 수 없으므로, 삭제도 200 + 공통 포맷으로 응답한다.

    // 7) DELETE: 한 개 삭제  → 200 / 없으면 404
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable Long id) {
        findOrThrow(id);
        store.remove(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 8) DELETE: 전체 삭제 (지운 개수를 반환)  → 200
    @DeleteMapping
    public ResponseEntity<ApiResponse<Map<String, Integer>>> deleteAllItems() {
        int count = store.size();
        store.clear();
        return ResponseEntity.ok(ApiResponse.success(Map.of("deletedCount", count)));
    }

    // ===== 공통 =====

    private ItemDto save(String name, Integer price) {
        ItemDto item = new ItemDto();
        item.setId(sequence++);
        item.setName(name);
        item.setPrice(price);
        store.put(item.getId(), item);
        return item;
    }

    // 없으면 NotFoundException → GlobalExceptionHandler가 404로 변환
    private ItemDto findOrThrow(Long id) {
        ItemDto item = store.get(id);
        if (item == null) {
            throw new NotFoundException("상품을 찾을 수 없습니다. id=" + id);
        }
        return item;
    }
}
