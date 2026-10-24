package com.onlinestore.product; // same package as the other product classes

// Thrown when someone tries to reserve more units than are in stock.
public class InsufficientStockException extends RuntimeException { // unchecked, so no throws clauses needed
    public InsufficientStockException(Product product, int requested) { // receives the product and the amount asked for
        super("Not enough stock for '" + product.getName() + "' (requested: " // start building the error message
                + requested + ", available: " + product.getStock() + ")"); // finish the message with the numbers
    } // end of constructor
} // end of class
