package com.onlinestore.product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Spring Data generates the implementation: save, findById, findAll, deleteById, ...
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByNameContainingIgnoreCase(String name);
}
