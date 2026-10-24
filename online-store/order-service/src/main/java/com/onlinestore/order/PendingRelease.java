package com.onlinestore.order; // same package as the other order classes

import jakarta.persistence.Entity; // marks a class as a database table
import jakarta.persistence.GeneratedValue; // lets the database generate the id
import jakarta.persistence.GenerationType; // the strategy used to generate ids
import jakarta.persistence.Id; // marks the primary key field

import java.time.Instant; // a point in time

// DIFFICULTY: a failed "give the stock back" call must survive crashes, so it is stored in the database, not in memory.
@Entity // JPA maps this class to a table
public class PendingRelease { // one stock release that still has to reach product-service

    @Id // primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // database auto-increments the id
    private Long id; // unique id of this retry task

    private Long productId; // which product needs its stock back
    private int quantity; // how many units to give back
    private int attempts; // how many times we already tried (used to give up eventually)
    private Instant createdAt = Instant.now(); // when the first failure happened

    protected PendingRelease() { // no-arg constructor
        // required by JPA
    } // end of constructor

    public PendingRelease(Long productId, int quantity) { // constructor used by app code
        this.productId = productId; // store the product
        this.quantity = quantity; // store the amount
    } // end of constructor

    public void recordFailedAttempt() { // called after a retry also failed
        attempts++; // count it so the job can stop after too many tries
    } // end of recordFailedAttempt

    public Long getId() { return id; } // getter for id
    public Long getProductId() { return productId; } // getter for product id
    public int getQuantity() { return quantity; } // getter for quantity
    public int getAttempts() { return attempts; } // getter for attempts
    public Instant getCreatedAt() { return createdAt; } // getter for creation time
} // end of class
