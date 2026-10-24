package com.onlinestore.product; // package this entity lives in

import jakarta.persistence.Entity; // marks a class as a database table
import jakarta.persistence.GeneratedValue; // lets the database generate the id
import jakarta.persistence.GenerationType; // the strategy used to generate ids
import jakarta.persistence.Id; // marks the primary key field
import jakarta.persistence.Version; // enables optimistic locking
import jakarta.validation.constraints.Min; // validation: number must be >= a minimum
import jakarta.validation.constraints.NotBlank; // validation: string must have text
import jakarta.validation.constraints.NotNull; // validation: value must not be null

import java.math.BigDecimal; // exact decimal type, correct for money

@Entity // JPA maps this class to a table
public class Product { // a product that the store sells

    @Id // this field is the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // database auto-increments the id
    private Long id; // unique product id

    @Version // Hibernate bumps this on every update and rejects stale writes
    private Long version; // lets two concurrent reservations be detected instead of overselling

    @NotBlank // name cannot be empty
    private String name; // display name

    @NotNull // price is required
    @Min(0) // price cannot be negative
    private BigDecimal price; // unit price

    @Min(0) // stock cannot be negative
    private int stock; // units currently available

    protected Product() { // no-arg constructor
        // required by JPA
    } // end of constructor

    public Product(String name, BigDecimal price, int stock) { // constructor used by app code
        this.name = name; // store the name
        this.price = price; // store the price
        this.stock = stock; // store the starting stock
    } // end of constructor

    // Takes units out of stock; refuses if there are not enough.
    public void reserve(int quantity) { // quantity = units the caller wants
        if (quantity > stock) { // more requested than available?
            throw new InsufficientStockException(this, quantity); // abort the reservation
        } // end of check
        stock -= quantity; // remove the reserved units from stock
    } // end of reserve

    // Puts units back into stock, e.g. when an order is cancelled.
    public void release(int quantity) { // quantity = units to give back
        stock += quantity; // add the units back
    } // end of release

    public Long getId() { return id; } // getter for id
    public Long getVersion() { return version; } // getter for the lock version
    public String getName() { return name; } // getter for name
    public void setName(String name) { this.name = name; } // setter for name
    public BigDecimal getPrice() { return price; } // getter for price
    public void setPrice(BigDecimal price) { this.price = price; } // setter for price
    public int getStock() { return stock; } // getter for stock
    public void setStock(int stock) { this.stock = stock; } // setter for stock
} // end of class
