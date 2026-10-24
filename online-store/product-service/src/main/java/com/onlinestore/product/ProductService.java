package com.onlinestore.product; // same package as the controller and repository

import org.springframework.stereotype.Service; // marks this class as a Spring service bean
import org.springframework.transaction.annotation.Transactional; // runs a method in one DB transaction

@Service // Spring creates one instance and injects it where needed
public class ProductService { // business logic for stock changes

    private final ProductRepository repository; // used to load and save products

    public ProductService(ProductRepository repository) { // Spring injects the repository
        this.repository = repository; // keep it for later use
    } // end of constructor

    @Transactional // load + change + save happen atomically; a failure rolls everything back
    public Product reserve(Long id, int quantity) { // take `quantity` units out of stock
        Product product = repository.findById(id) // look the product up
                .orElseThrow(() -> new ProductNotFoundException(id)); // 404 if it does not exist
        product.reserve(quantity); // domain rule: throws if stock is too low
        return repository.save(product); // persist; @Version detects concurrent changes
    } // end of reserve

    @Transactional // same atomic guarantee for giving stock back
    public Product release(Long id, int quantity) { // put `quantity` units back into stock
        Product product = repository.findById(id) // look the product up
                .orElseThrow(() -> new ProductNotFoundException(id)); // 404 if it does not exist
        product.release(quantity); // add the units back
        return repository.save(product); // persist the new stock level
    } // end of release
} // end of class
