package com.onlinestore.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

// The part of product-service's response that order-service cares about.
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductDto(Long id, String name, BigDecimal price, int stock) {
}
