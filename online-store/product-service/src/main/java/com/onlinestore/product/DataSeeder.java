package com.onlinestore.product;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DataSeeder {

    // Runs once at startup so the store is not empty (H2 is in-memory and resets on restart).
    @Bean
    CommandLineRunner seedProducts(ProductRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                repository.saveAll(List.of(
                        new Product("Mechanical Keyboard", new BigDecimal("89.99"), 25),
                        new Product("Wireless Mouse", new BigDecimal("29.50"), 60),
                        new Product("27\" Monitor", new BigDecimal("249.00"), 10),
                        new Product("USB-C Hub", new BigDecimal("39.90"), 40),
                        new Product("Laptop Stand", new BigDecimal("34.00"), 30)));
            }
        };
    }
}
