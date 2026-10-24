package com.onlinestore.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

// Java records are compact immutable data carriers - ideal for request/response DTOs.
public record CreateOrderRequest(@NotEmpty List<@Valid Item> items) {

    public record Item(@NotNull Long productId, @Min(1) int quantity) {
    }
}
