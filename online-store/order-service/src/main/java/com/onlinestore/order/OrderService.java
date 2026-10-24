package com.onlinestore.order; // same package as the other order classes

import org.springframework.stereotype.Service; // marks this class as a Spring service bean
import org.springframework.transaction.annotation.Transactional; // runs a method in one DB transaction

import java.util.ArrayList; // growable list implementation
import java.util.List; // list interface

@Service // Spring creates one instance
public class OrderService { // business logic for orders

    private final OrderRepository repository; // saves and loads orders
    private final ProductClient productClient; // talks to product-service
    private final PendingReleaseService pendingReleases; // remembers releases that failed so they can be retried

    public OrderService(OrderRepository repository, ProductClient productClient, PendingReleaseService pendingReleases) { // Spring injects all three
        this.repository = repository; // keep the repository
        this.productClient = productClient; // keep the client
        this.pendingReleases = pendingReleases; // keep the retry store
    } // end of constructor

    @Transactional // if anything throws, the order row is rolled back too
    public Order create(CreateOrderRequest request) { // build, reserve stock for, and save a new order
        Order order = new Order(); // start with an empty PENDING order
        List<OrderLine> reserved = new ArrayList<>(); // lines whose stock we already took, in case we must undo
        try { // everything below may fail halfway through
            for (CreateOrderRequest.Item item : request.items()) { // handle each requested item
                ProductDto product = productClient.findById(item.productId()) // look the product up
                        .orElseThrow(() -> new InvalidOrderException("Product " + item.productId() + " does not exist")); // unknown id
                if (product.stock() < item.quantity()) { // quick check before the real reservation
                    throw new InvalidOrderException( // reject with a helpful message
                            "Not enough stock for '" + product.name() + "' (available: " + product.stock() + ")"); // show availability
                } // end of stock check
                productClient.reserve(product.id(), item.quantity()); // really take the units (atomic on the product side)
                OrderLine line = new OrderLine(product.id(), product.name(), product.price(), item.quantity()); // snapshot the product data
                reserved.add(line); // remember it so it can be rolled back
                order.addLine(line); // attach it to the order
            } // end of loop
            return repository.save(order); // persist the finished order
        } catch (RuntimeException e) { // any failure: product missing, stock gone, DB error, ...
            releaseAll(reserved); // compensate: give back every unit we reserved so far
            throw e; // rethrow so the client still sees the original error
        } // end of try/catch
    } // end of create

    @Transactional(readOnly = true) // no writes, lets the database optimise
    public List<Order> findAll() { // list every order
        return repository.findAll(); // delegate to the repository
    } // end of findAll

    @Transactional(readOnly = true) // no writes
    public Order get(Long id) { // load one order
        return repository.findById(id).orElseThrow(() -> new OrderNotFoundException(id)); // 404 if missing
    } // end of get

    @Transactional // status change must be saved atomically
    public Order pay(Long id) { // PENDING -> PAID
        Order order = get(id); // load the order
        order.moveTo(OrderStatus.PAID); // validates the transition
        return repository.save(order); // persist the new status
    } // end of pay

    @Transactional // status change must be saved atomically
    public Order ship(Long id) { // PAID -> SHIPPED
        Order order = get(id); // load the order
        order.moveTo(OrderStatus.SHIPPED); // validates the transition
        return repository.save(order); // persist the new status
    } // end of ship

    @Transactional // status change and stock return belong together
    public Order cancel(Long id) { // PENDING or PAID -> CANCELLED
        Order order = get(id); // load the order
        order.moveTo(OrderStatus.CANCELLED); // validates the transition (shipped orders cannot be cancelled)
        releaseAll(order.getLines()); // return the reserved stock to the shop
        return repository.save(order); // persist the new status
    } // end of cancel

    // Best-effort undo: a failure on one line must not stop the others from being released.
    private void releaseAll(List<OrderLine> lines) { // lines = what to give back
        for (OrderLine line : lines) { // go through every line
            try { // isolate each call
                productClient.release(line.getProductId(), line.getQuantity()); // return the units
            } catch (RuntimeException e) { // product-service might be down; keep going
                pendingReleases.record(line.getProductId(), line.getQuantity()); // queue it (own transaction) so the stock is not lost
            } // end of try/catch
        } // end of loop
    } // end of releaseAll
} // end of class
