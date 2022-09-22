package com.jvbarbosa.dscommerce.services;

import com.jvbarbosa.dscommerce.dto.OrderDTO;
import com.jvbarbosa.dscommerce.entities.Order;
import com.jvbarbosa.dscommerce.repositories.OrderRepository;
import com.jvbarbosa.dscommerce.services.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository repository;

    @Transactional(readOnly = true)
    public OrderDTO findById(Long id) {
        Order order = repository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Recurso não encontrado"));
        return new OrderDTO(order);
    }
}
