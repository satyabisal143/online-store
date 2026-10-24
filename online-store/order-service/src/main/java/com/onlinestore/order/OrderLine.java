package com.onlinestore.order;

import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

// One line of an order. Name and price are copied at order time so later product edits don't rewrite history.
@Embeddable
public class OrderLine {

    private Long productId;
    private String productName;
    private BigDecimal unitPrice;
    private int quantity;

    protected OrderLine() {
    }

    public OrderLine(Long productId, String productName, BigDecimal unitPrice, int quantity) {
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
