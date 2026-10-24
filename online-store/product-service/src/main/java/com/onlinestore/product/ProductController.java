package com.onlinestore.product;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository repository;
    private final ProductService service; // handles the transactional stock operations

    public ProductController(ProductRepository repository, ProductService service) { // Spring injects both
        this.repository = repository; // keep the repository for CRUD
        this.service = service; // keep the service for reserve/release
    }

    // POST /api/products/{id}/reserve?quantity=N - atomically takes N units out of stock.
    @PostMapping("/{id}/reserve") // maps the URL and HTTP method
    public Product reserve(@PathVariable Long id, @RequestParam int quantity) { // id from path, quantity from query
        return service.reserve(id, quantity); // delegate; returns the product with its new stock
    }

    // POST /api/products/{id}/release?quantity=N - puts N units back into stock.
    @PostMapping("/{id}/release") // maps the URL and HTTP method
    public Product release(@PathVariable Long id, @RequestParam int quantity) { // id from path, quantity from query
        return service.release(id, quantity); // delegate; returns the product with its new stock
    }

    @GetMapping
    public List<Product> list(@RequestParam(required = false) String search) {
        return search == null || search.isBlank()
                ? repository.findAll()
                : repository.findByNameContainingIgnoreCase(search);
    }

    @GetMapping("/{id}")
    public Product get(@PathVariable Long id) {
        return repository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product create(@Valid @RequestBody Product product) {
        return repository.save(new Product(product.getName(), product.getPrice(), product.getStock()));
    }

    @PutMapping("/{id}")
    public Product update(@PathVariable Long id, @Valid @RequestBody Product update) {
        Product existing = get(id);
        existing.setName(update.getName());
        existing.setPrice(update.getPrice());
        existing.setStock(update.getStock());
        return repository.save(existing);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        repository.deleteById(id);
    }
}
