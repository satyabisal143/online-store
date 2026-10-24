package com.onlinestore.order;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidOrderException.class)
    public ProblemDetail invalidOrder(InvalidOrderException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(OrderNotFoundException.class) // unknown order id
    public ProblemDetail notFound(OrderNotFoundException ex) { // build the error body
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage()); // 404 Not Found
    }
}
