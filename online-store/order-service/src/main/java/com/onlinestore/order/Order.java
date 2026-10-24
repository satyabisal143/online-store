package com.onlinestore.order; // package this entity lives in

import jakarta.persistence.ElementCollection; // maps a collection of value objects to its own table
import jakarta.persistence.Entity; // marks a class as a database table
import jakarta.persistence.EnumType; // controls how enums are stored
import jakarta.persistence.Enumerated; // marks an enum field
import jakarta.persistence.FetchType; // eager vs lazy loading
import jakarta.persistence.GeneratedValue; // lets the database generate the id
import jakarta.persistence.GenerationType; // the strategy used to generate ids
import jakarta.persistence.Id; // marks the primary key field
import jakarta.persistence.Table; // sets a custom table name

import java.math.BigDecimal; // exact decimal type, correct for money
import java.time.Instant; // a point in time
import java.util.ArrayList; // growable list implementation
import java.util.List; // list interface

// "order" is a reserved SQL word, so the table is called "orders".
@Entity // JPA maps this class to a table
@Table(name = "orders") // use the table name "orders"
public class Order { // a customer order

    @Id // primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // database auto-increments the id
    private Long id; // unique order id

    private Instant createdAt = Instant.now(); // when the order was placed

    private BigDecimal total = BigDecimal.ZERO; // sum of all line totals

    @Enumerated(EnumType.STRING) // store the name ("PAID") instead of a fragile ordinal number
    private OrderStatus status = OrderStatus.PENDING; // every order starts as PENDING

    @ElementCollection(fetch = FetchType.EAGER) // lines are loaded together with the order
    private List<OrderLine> lines = new ArrayList<>(); // the items in this order

    public Long getId() { return id; } // getter for id
    public Instant getCreatedAt() { return createdAt; } // getter for creation time
    public BigDecimal getTotal() { return total; } // getter for total
    public OrderStatus getStatus() { return status; } // getter for status
    public List<OrderLine> getLines() { return lines; } // getter for lines

    public void addLine(OrderLine line) { // add an item to the order
        lines.add(line); // store the line
        total = total.add(line.lineTotal()); // keep the running total up to date
    } // end of addLine

    // Moves the order to a new state, but only if the workflow allows it.
    public void moveTo(OrderStatus target) { // target = the state we want
        if (!status.canMoveTo(target)) { // is this transition illegal?
            throw new InvalidOrderException( // reject it with a clear message
                    "Cannot change order from " + status + " to " + target); // e.g. SHIPPED -> PAID
        } // end of check
        status = target; // apply the transition
    } // end of moveTo
} // end of class
