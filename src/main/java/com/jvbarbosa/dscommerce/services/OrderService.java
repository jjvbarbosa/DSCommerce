package com.jvbarbosa.dscommerce.services;

import com.jvbarbosa.dscommerce.dto.OrderDTO;
import com.jvbarbosa.dscommerce.dto.OrderItemDTO;
import com.jvbarbosa.dscommerce.entities.Order;
import com.jvbarbosa.dscommerce.entities.OrderItem;
import com.jvbarbosa.dscommerce.entities.Product;
import com.jvbarbosa.dscommerce.entities.User;
import com.jvbarbosa.dscommerce.entities.enums.OrderStatus;
import com.jvbarbosa.dscommerce.repositories.OrderItemRepository;
import com.jvbarbosa.dscommerce.repositories.OrderRepository;
import com.jvbarbosa.dscommerce.repositories.ProductRepository;
import com.jvbarbosa.dscommerce.services.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class OrderService {

    @Autowired
    private OrderRepository repository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private UserService userService;

    @Transactional(readOnly = true)
    public OrderDTO findById(Long id) {
        Order order = repository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Recurso não encontrado"));
        return new OrderDTO(order);
    }

    @Transactional
    public OrderDTO insert(OrderDTO dto) {
        Order order = new Order();

        order.setMoment(Instant.now());
        order.setStatus(OrderStatus.WAITING_PAYMENT);

        User user = userService.authenticated();
        order.setClient(user);

        for (OrderItemDTO itemDto : dto.getItems()) {
            Product product = productRepository.getReferenceById(itemDto.getProductId());
            OrderItem item = new OrderItem(order, product, itemDto.getQuantity(), product.getPrice());
            order.getItems().add(item);
        }

        repository.save(order);
        orderItemRepository.saveAll(order.getItems());

        return new OrderDTO(order);
    }
}
