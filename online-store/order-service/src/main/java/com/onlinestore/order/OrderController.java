package com.onlinestore.order; // same package as the other order classes

import jakarta.validation.Valid; // triggers bean validation on the request body
import org.springframework.http.HttpStatus; // HTTP status codes
import org.springframework.web.bind.annotation.GetMapping; // maps HTTP GET
import org.springframework.web.bind.annotation.PathVariable; // reads a value from the URL path
import org.springframework.web.bind.annotation.PostMapping; // maps HTTP POST
import org.springframework.web.bind.annotation.RequestBody; // reads the JSON body
import org.springframework.web.bind.annotation.RequestMapping; // sets the base URL
import org.springframework.web.bind.annotation.ResponseStatus; // sets the response status
import org.springframework.web.bind.annotation.RestController; // marks a REST controller

import java.util.List; // list interface

@RestController // every method returns JSON
@RequestMapping("/api/orders") // base URL for all endpoints below
public class OrderController { // HTTP layer for orders

    private final OrderService service; // business logic

    public OrderController(OrderService service) { // Spring injects the service
        this.service = service; // keep it
    } // end of constructor

    @GetMapping // GET /api/orders
    public List<Order> list() { // all orders
        return service.findAll(); // delegate
    } // end of list

    @GetMapping("/{id}") // GET /api/orders/{id}
    public Order get(@PathVariable Long id) { // one order
        return service.get(id); // delegate
    } // end of get

    @PostMapping // POST /api/orders
    @ResponseStatus(HttpStatus.CREATED) // answer 201 on success
    public Order create(@Valid @RequestBody CreateOrderRequest request) { // validated JSON body
        return service.create(request); // delegate
    } // end of create

    @PostMapping("/{id}/pay") // POST /api/orders/{id}/pay
    public Order pay(@PathVariable Long id) { // mark as paid
        return service.pay(id); // delegate
    } // end of pay

    @PostMapping("/{id}/ship") // POST /api/orders/{id}/ship
    public Order ship(@PathVariable Long id) { // mark as shipped
        return service.ship(id); // delegate
    } // end of ship

    @PostMapping("/{id}/cancel") // POST /api/orders/{id}/cancel
    public Order cancel(@PathVariable Long id) { // cancel and restock
        return service.cancel(id); // delegate
    } // end of cancel
} // end of class
