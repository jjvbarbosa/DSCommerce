package com.jvbarbosa.dscommerce.services;

import com.jvbarbosa.dscommerce.dto.OrderDTO;
import com.jvbarbosa.dscommerce.entities.Order;
import com.jvbarbosa.dscommerce.entities.OrderItem;
import com.jvbarbosa.dscommerce.entities.Product;
import com.jvbarbosa.dscommerce.entities.User;
import com.jvbarbosa.dscommerce.factories.OrderFactory;
import com.jvbarbosa.dscommerce.factories.ProductFactory;
import com.jvbarbosa.dscommerce.factories.UserFactory;
import com.jvbarbosa.dscommerce.repositories.OrderItemRepository;
import com.jvbarbosa.dscommerce.repositories.OrderRepository;
import com.jvbarbosa.dscommerce.repositories.ProductRepository;
import com.jvbarbosa.dscommerce.services.exceptions.ForbiddenException;
import com.jvbarbosa.dscommerce.services.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class OrderServiceTests {

    @InjectMocks
    private OrderService service;

    @Mock
    private OrderRepository repository;

    @Mock
    private UserService userService;

    @Mock
    private AuthService authService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    private Long existingOrderId, nonExistingOrderId;
    private Long existingProductId, nonExistingProductId;
    private User admin, client;
    private Order order;
    private OrderDTO orderDTO;
    private Product product;


    @BeforeEach
    void setUp() throws Exception {
        existingOrderId = 1L;
        nonExistingOrderId = 2L;

        existingProductId = 1L;
        nonExistingProductId = 2L;

        admin = UserFactory.createCustomAdminUser(1L, "João");
        client = UserFactory.createCustomClientUser(2L, "Mariana");

        order = OrderFactory.createOrder(client);

        orderDTO = new OrderDTO(order);

        product = ProductFactory.createProduct();

        when(repository.findById(existingOrderId)).thenReturn(Optional.of(order));
        when(repository.findById(nonExistingOrderId)).thenReturn(Optional.empty());

        when(repository.save(any())).thenReturn(order);

        when(productRepository.getReferenceById(existingProductId)).thenReturn(product);
        when(productRepository.getReferenceById(nonExistingProductId)).thenThrow(EntityNotFoundException.class);

        when(orderItemRepository.saveAll(any())).thenReturn(new ArrayList<>(order.getItems()));
    }

    @Test
    public void findByIdShouldReturnOrderDTOWhenIdExistsAndAdminLogged() {
        doNothing().when(authService).validateSelfOrAdmin(any());

        OrderDTO result = service.findById(existingOrderId);

        assertNotNull(result);
        assertEquals(result.getId(), existingOrderId);
    }

    @Test
    public void findByIdShouldReturnOrderDTOWhenIdExistsAndSelfClientLogged() {
        doNothing().when(authService).validateSelfOrAdmin(any());

        OrderDTO result = service.findById(existingOrderId);

        assertNotNull(result);
        assertEquals(result.getId(), existingOrderId);
    }

    @Test
    public void findByIdShouldThrowsForbiddenExceptionWhenIdExistsAndOtherClientLogged() {
        doThrow(ForbiddenException.class).when(authService).validateSelfOrAdmin(any());

        assertThrows(ForbiddenException.class, () -> {
            service.findById(existingOrderId);
        });
    }

    @Test
    public void findByIdShouldThrowsResourceNotFoundExceptionWhenIdDoesNotExists() {
        doNothing().when(authService).validateSelfOrAdmin(any());

        assertThrows(ResourceNotFoundException.class, () -> {
            service.findById(nonExistingOrderId);
        });
    }

    @Test
    public void insertShouldReturnOrderDTOWhenAdminLogged() {
        when(userService.authenticated()).thenReturn(admin);

        OrderDTO result = service.insert(orderDTO);

        assertNotNull(result);
    }

    @Test
    public void insertShouldReturnOrderDTOWhenClientLogged() {
        when(userService.authenticated()).thenReturn(client);

        OrderDTO result = service.insert(orderDTO);

        assertNotNull(result);
    }

    @Test
    public void insertShouldThrowsUsernameNotFoundExceptionWhenUserNotLogged() {
        doThrow(UsernameNotFoundException.class).when(userService).authenticated();

        order.setClient(new User());
        orderDTO = new OrderDTO(order);

        assertThrows(UsernameNotFoundException.class, () -> {
            service.insert(orderDTO);
        });
    }

    @Test
    public void insertShouldThrowsEntityNotFoundExceptionWhenOrderProductIdDoesNotExists() {
        when(userService.authenticated()).thenReturn(client);

        product.setId(nonExistingProductId);
        OrderItem orderItem = new OrderItem(order, product, 2, 4269.90);
        order.getItems().add(orderItem);

        orderDTO = new OrderDTO(order);

        assertThrows(EntityNotFoundException.class, () -> {
            service.insert(orderDTO);
        });
    }
}
