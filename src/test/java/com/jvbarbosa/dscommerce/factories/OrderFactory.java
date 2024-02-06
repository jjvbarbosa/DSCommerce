package com.jvbarbosa.dscommerce.factories;

import com.jvbarbosa.dscommerce.entities.*;
import com.jvbarbosa.dscommerce.entities.enums.OrderStatus;

import java.time.Instant;

public class OrderFactory {

    public static Order createOrder(User client) {
        Order order = new Order(
                1L,
                Instant.now(),
                OrderStatus.WAITING_PAYMENT,
                client,
                new Payment()
        );

        Product product = ProductFactory.createProduct();
        OrderItem orderItem = new OrderItem(order, product, 2, 4269.90);

        order.getItems().add(orderItem);

        return order;
    }
}
