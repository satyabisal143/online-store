package com.onlinestore.product;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    ProductRepository repository;

    @MockitoBean
    ProductService service; // the controller now depends on it

    @Test
    void reserveWithTooLittleStockReturns409() throws Exception {
        when(service.reserve(1L, 5)).thenThrow(
                new InsufficientStockException(new Product("Mouse", new BigDecimal("10"), 1), 5));

        mvc.perform(post("/api/products/1/reserve").param("quantity", "5"))
                .andExpect(status().isConflict());
    }

    @Test
    void listReturnsProducts() throws Exception {
        when(repository.findAll()).thenReturn(List.of(new Product("Mouse", new BigDecimal("10"), 5)));

        mvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Mouse"));
    }

    @Test
    void missingProductReturns404() throws Exception {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        mvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidProductIsRejected() throws Exception {
        mvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"price\":5,\"stock\":1}"))
                .andExpect(status().isBadRequest());
    }
}
