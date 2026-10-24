package com.onlinestore.order;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    OrderRepository repository;

    @Mock
    ProductClient productClient;

    @Mock
    PendingReleaseService pendingReleases;

    @InjectMocks
    OrderService service;

    @Test
    void failedReleaseIsQueuedForRetry() {
        Order order = new Order();
        order.addLine(new OrderLine(1L, "Mouse", new BigDecimal("10"), 3));
        when(repository.findById(7L)).thenReturn(Optional.of(order));
        when(repository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        doThrow(new RuntimeException("product-service down")).when(productClient).release(1L, 3); // release fails

        service.cancel(7L); // cancelling must still succeed

        verify(pendingReleases).record(1L, 3); // the failed release was stored for the retry job
    }

    @Test
    void totalIsSumOfLines() {
        when(productClient.findById(1L)).thenReturn(Optional.of(new ProductDto(1L, "Mouse", new BigDecimal("10.00"), 5)));
        when(productClient.findById(2L)).thenReturn(Optional.of(new ProductDto(2L, "Keyboard", new BigDecimal("25.50"), 5)));
        when(repository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order order = service.create(new CreateOrderRequest(List.of(
                new CreateOrderRequest.Item(1L, 2),
                new CreateOrderRequest.Item(2L, 1))));

        assertThat(order.getTotal()).isEqualByComparingTo("45.50");
        assertThat(order.getLines()).hasSize(2);
    }

    @Test
    void failureOnSecondLineReleasesFirstLine() {
        when(productClient.findById(1L)).thenReturn(Optional.of(new ProductDto(1L, "Mouse", new BigDecimal("10"), 5))); // line 1 is fine
        when(productClient.findById(2L)).thenReturn(Optional.empty()); // line 2 does not exist

        assertThatThrownBy(() -> service.create(new CreateOrderRequest(List.of(
                new CreateOrderRequest.Item(1L, 2),
                new CreateOrderRequest.Item(2L, 1)))))
                .isInstanceOf(InvalidOrderException.class);

        verify(productClient).reserve(1L, 2); // line 1 stock was taken...
        verify(productClient).release(1L, 2); // ...and given back after the failure
    }

    @Test
    void cancelReleasesStockAndShippedOrderCannotBeCancelled() {
        Order order = new Order();
        order.addLine(new OrderLine(1L, "Mouse", new BigDecimal("10"), 3));
        when(repository.findById(7L)).thenReturn(Optional.of(order));
        when(repository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order cancelled = service.cancel(7L);

        assertThat(cancelled.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(productClient).release(1L, 3);
        assertThatThrownBy(() -> service.ship(7L)).isInstanceOf(InvalidOrderException.class); // CANCELLED -> SHIPPED is illegal
    }

    @Test
    void unknownProductIsRejected() {
        when(productClient.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(new CreateOrderRequest(List.of(new CreateOrderRequest.Item(9L, 1)))))
                .isInstanceOf(InvalidOrderException.class);
    }

    @Test
    void insufficientStockIsRejected() {
        when(productClient.findById(1L)).thenReturn(Optional.of(new ProductDto(1L, "Mouse", new BigDecimal("10"), 1)));

        assertThatThrownBy(() -> service.create(new CreateOrderRequest(List.of(new CreateOrderRequest.Item(1L, 3)))))
                .isInstanceOf(InvalidOrderException.class)
                .hasMessageContaining("Not enough stock");
    }
}
