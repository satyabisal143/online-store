package com.onlinestore.order; // same package as the order classes

import java.util.Set; // used to list allowed next states

// The life cycle of an order, with the legal moves between states.
public enum OrderStatus { // each constant is one state

    SHIPPED(Set.of()), // final state: nothing can follow (declared first so later states can refer to it)
    CANCELLED(Set.of()), // final state: nothing can follow
    PAID(Set.of(SHIPPED, CANCELLED)), // a paid order can be shipped or cancelled
    PENDING(Set.of(PAID, CANCELLED)); // a new order can be paid or cancelled

    private final Set<OrderStatus> allowedNext; // states reachable from this one

    OrderStatus(Set<OrderStatus> allowedNext) { // enum constructor, called once per constant
        this.allowedNext = allowedNext; // remember the allowed transitions
    } // end of constructor

    public boolean canMoveTo(OrderStatus target) { // is the move this -> target legal?
        return allowedNext.contains(target); // true only if target is in the allowed set
    } // end of canMoveTo
} // end of enum
