package com.onlinestore.order; // same package as the order classes

// Thrown when an order id does not exist.
public class OrderNotFoundException extends RuntimeException { // unchecked exception
    public OrderNotFoundException(Long id) { // receives the missing id
        super("Order " + id + " not found"); // message shown to the client
    } // end of constructor
} // end of class
