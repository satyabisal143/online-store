package com.onlinestore.order; // same package as the order classes

import org.springframework.beans.factory.annotation.Value; // injects values from application.yml
import org.springframework.stereotype.Component; // marks this class as a Spring bean
import org.springframework.web.client.HttpClientErrorException; // thrown for HTTP 4xx answers
import org.springframework.web.client.RestClient; // Spring's modern HTTP client

import java.util.Optional; // container that may or may not hold a value

// Calls product-service over HTTP. In production, URLs come from service discovery (see RestClientConfig).
@Component // Spring creates one instance
public class ProductClient { // gateway to the product-service API

    private final RestClient restClient; // the configured HTTP client

    public ProductClient(RestClient.Builder builder, @Value("${product-service.url}") String baseUrl) { // injected
        this.restClient = builder.baseUrl(baseUrl).build(); // build a client that targets product-service
    } // end of constructor

    public Optional<ProductDto> findById(Long id) { // fetch one product
        try { // a 404 is a normal outcome here, not a crash
            return Optional.ofNullable( // wrap the possibly-null body
                    restClient.get().uri("/api/products/{id}", id).retrieve().body(ProductDto.class)); // GET + parse JSON
        } catch (HttpClientErrorException.NotFound e) { // product-service answered 404
            return Optional.empty(); // report "no such product"
        } // end of try/catch
    } // end of findById

    // Asks product-service to take units out of stock. Fails with InvalidOrderException if it refuses.
    public void reserve(Long id, int quantity) { // id = product, quantity = units wanted
        try { // a 409 means not enough stock
            restClient.post() // send an HTTP POST
                    .uri("/api/products/{id}/reserve?quantity={q}", id, quantity) // fill in the URL placeholders
                    .retrieve() // execute the request
                    .toBodilessEntity(); // we do not need the response body
        } catch (HttpClientErrorException.Conflict e) { // product-service said 409
            throw new InvalidOrderException("Could not reserve " + quantity + " of product " + id + ": stock changed"); // surface it as a business error
        } // end of try/catch
    } // end of reserve

    // Gives units back to product-service, used when an order fails or is cancelled.
    public void release(Long id, int quantity) { // id = product, quantity = units to return
        restClient.post() // send an HTTP POST
                .uri("/api/products/{id}/release?quantity={q}", id, quantity) // fill in the URL placeholders
                .retrieve() // execute the request
                .toBodilessEntity(); // no body needed
    } // end of release
} // end of class
