package com.example.ksl.wsd2025.api.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class PriceUpdateRequest {
    @NotNull(message = "price는 필수입니다.")
    @PositiveOrZero(message = "price는 0 이상이어야 합니다.")
    private Integer price;

    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }
}
