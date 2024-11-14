package com.jvbarbosa.dscommerce.controllers.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jvbarbosa.dscommerce.dto.OrderDTO;
import com.jvbarbosa.dscommerce.dto.ProductDTO;
import com.jvbarbosa.dscommerce.entities.*;
import com.jvbarbosa.dscommerce.entities.enums.OrderStatus;
import com.jvbarbosa.dscommerce.factories.ProductFactory;
import com.jvbarbosa.dscommerce.factories.UserFactory;
import com.jvbarbosa.dscommerce.utils.TokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class OrderControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TokenUtil tokenUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String clientToken;
    private String invalidToken;

    private String adminUsername;
    private String adminPassword;

    private String clientUsername;
    private String clientPassword;

    private Long existingId;
    private Long nonExistingId;
    private Long otherExistingId;

    private User user;
    private Product product;
    private Order order;
    private OrderItem orderItem;
    private OrderDTO orderDTO;

    @BeforeEach
    void setUp() throws Exception {

        adminUsername = "alex@gmail.com";
        adminPassword = "123456";

        clientUsername = "maria@gmail.com";
        clientPassword = "123456";

        existingId = 1L;
        nonExistingId = 999L;
        otherExistingId = 2L;

        adminToken = tokenUtil.obtainAccessToken(mockMvc, adminUsername,adminPassword);
        clientToken = tokenUtil.obtainAccessToken(mockMvc, clientUsername, clientPassword);
        invalidToken = adminToken + "xpto"; // Simulates wrong password

        product = ProductFactory.createProduct();

        user = UserFactory.createClientUser();
        order = new Order(
                null,
                Instant.now(),
                OrderStatus.WAITING_PAYMENT,
                user,
                null
        );

        orderItem = new OrderItem(order, product, 2, 8539.80);

        order.getItems().add(orderItem);
    }

    @Test
    public void findByIdShouldReturnOrderDTOWhenIdExistAndAdminLoggedIn() throws Exception {

        ResultActions result = mockMvc
                .perform(get("/orders/{id}", existingId)
                        .header("Authorization", "Bearer " + adminToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print());
        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$.id").value(existingId));
        result.andExpect(jsonPath("$.moment").value("2022-07-25T13:00:00Z"));
        result.andExpect(jsonPath("$.status").value("PAID"));
        result.andExpect(jsonPath("$.client").exists());
        result.andExpect(jsonPath("$.client.id").value(1L));
        result.andExpect(jsonPath("$.client.name").value("Maria Brown"));
        result.andExpect(jsonPath("$.payment").exists());
        result.andExpect(jsonPath("$.items").exists());
        result.andExpect(jsonPath("$.items[0].productId").value(1L));
        result.andExpect(jsonPath("$.items[0].name").value("The Lord of the Rings"));
        result.andExpect(jsonPath("$.items[1].productId").value(3L));
        result.andExpect(jsonPath("$.items[1].name").value("Macbook Pro"));
        result.andExpect(jsonPath("$.total").exists());
    }

    @Test
    public void findByIdShouldReturnOrderDTOWhenIdExistAndClientLoggedIn() throws Exception {

        ResultActions result = mockMvc
                .perform(get("/orders/{id}", existingId)
                        .header("Authorization", "Bearer " + clientToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print());
        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$.id").value(existingId));
        result.andExpect(jsonPath("$.moment").value("2022-07-25T13:00:00Z"));
        result.andExpect(jsonPath("$.status").value("PAID"));
        result.andExpect(jsonPath("$.client").exists());
        result.andExpect(jsonPath("$.client.id").value(1L));
        result.andExpect(jsonPath("$.client.name").value("Maria Brown"));
        result.andExpect(jsonPath("$.payment").exists());
        result.andExpect(jsonPath("$.items").exists());
        result.andExpect(jsonPath("$.items[0].productId").value(1L));
        result.andExpect(jsonPath("$.items[0].name").value("The Lord of the Rings"));
        result.andExpect(jsonPath("$.items[1].productId").value(3L));
        result.andExpect(jsonPath("$.items[1].name").value("Macbook Pro"));
        result.andExpect(jsonPath("$.total").exists());
    }

    @Test
    public void findByIdShouldReturnForbiddenOWhenIdExistAndClientLoggedInAndOrderDoesNotBelongUser() throws Exception {

        ResultActions result = mockMvc
                .perform(get("/orders/{id}", otherExistingId)
                        .header("Authorization", "Bearer " + clientToken)
                        .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isForbidden());
    }

    @Test
    public void findByIdShouldReturnNotFoundWhenIdDoesNotExistAndAdminLoggedIn() throws Exception {

        ResultActions result = mockMvc
                .perform(get("/orders/{id}", nonExistingId)
                        .header("Authorization", "Bearer " + adminToken)
                        .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isNotFound());
    }

    @Test
    public void findByIdShouldReturnNotFoundWhenIdDoesNotExistAndClientLoggedIn() throws Exception {

        ResultActions result = mockMvc
                .perform(get("/orders/{id}", nonExistingId)
                        .header("Authorization", "Bearer " + clientToken)
                        .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isNotFound());
    }

    @Test
    public void findByIdShouldReturnUnauthorizedWhenWhenTokenIsInvalid() throws Exception {

        ResultActions result = mockMvc
                .perform(get("/orders/{id}", existingId)
                        .header("Authorization", "Bearer " + invalidToken)
                        .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnauthorized());
    }
}
