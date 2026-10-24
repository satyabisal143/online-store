package com.onlinestore.product;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Turns exceptions into clean JSON error responses instead of stack traces.
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public ProblemDetail notFound(ProductNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(InsufficientStockException.class) // not enough units to reserve
    public ProblemDetail insufficientStock(InsufficientStockException ex) { // build the error body
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage()); // 409 Conflict
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class) // two requests changed the same row at once
    public ProblemDetail concurrentUpdate(ObjectOptimisticLockingFailureException ex) { // build the error body
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Product was modified concurrently, retry"); // 409
    }
}
