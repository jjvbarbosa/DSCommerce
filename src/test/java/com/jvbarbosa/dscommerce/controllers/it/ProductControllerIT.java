package com.jvbarbosa.dscommerce.controllers.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jvbarbosa.dscommerce.dto.ProductDTO;
import com.jvbarbosa.dscommerce.entities.Category;
import com.jvbarbosa.dscommerce.entities.Product;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ProductControllerIT {

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
    private Long dependentId;

    private String productName;
    private Product product;
    private ProductDTO productDTO;
    private Category category;

    @BeforeEach
    void setUp() throws Exception {

        productName = "Macbook";

        category = new Category(2L, "Games");
        product = new Product(
                null,
                "Console PlayStation 5",
                "Jogue como nunca antes. Curta uma nova geração de jogos incríveis do PlayStation",
                4269.90,
                "playstation-5.png"
        );
        product.getCategories().add(category);

        productDTO = new ProductDTO(product);

        adminUsername = "alex@gmail.com";
        adminPassword = "123456";

        clientUsername = "maria@gmail.com";
        clientPassword = "123456";

        existingId = 1L;
        nonExistingId = 999L;
        dependentId = 3L;

        adminToken = tokenUtil.obtainAccessToken(mockMvc, adminUsername,adminPassword);
        clientToken = tokenUtil.obtainAccessToken(mockMvc, clientUsername, clientPassword);
        invalidToken = adminToken + "xpto"; // Simulates wrong password
    }

    @Test
    public void findByIdShouldReturnProductDTOWhenIdExist() throws Exception {

        ResultActions result = mockMvc
                .perform(get("/products/{id}", existingId)
                        .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$.id").value(existingId));
        result.andExpect(jsonPath("$.name").value("The Lord of the Rings"));
        result.andExpect(jsonPath("$.price").value(90.5));
        result.andExpect(jsonPath("$.categories[0].id").value(1L));
        result.andExpect(jsonPath("$.categories[0].name").value("Livros"));
    }

    @Test
    public void findByIdShouldThrowNotFoundWhenIdDoesNotExist() throws Exception {

        ResultActions result = mockMvc
                .perform(get("/products/{id}", nonExistingId)
                        .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isNotFound());
    }

    @Test
    public void findAllShouldReturnPageWhenNameParameterIsProvided() throws Exception {

        ResultActions result = mockMvc
                .perform(get("/products?name={productName}", productName)
                        .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$.content[0].id").value(3L));
        result.andExpect(jsonPath("$.content[0].name").value("Macbook Pro"));
        result.andExpect(jsonPath("$.content[0].price").value(1250.0));
        result.andExpect(jsonPath("$.content[0].imgUrl").value("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/3-big.jpg"));
    }

    @Test
    public void findAllShouldReturnPageWhenNameParameterIsEmpty() throws Exception {

        ResultActions result = mockMvc
                .perform(get("/products")
                        .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$.content[0].id").value(1L));
        result.andExpect(jsonPath("$.content[0].name").value("The Lord of the Rings"));
        result.andExpect(jsonPath("$.content[0].price").value(90.50));
        result.andExpect(jsonPath("$.content[0].imgUrl").value("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/1-big.jpg"));
    }

    @Test
    public void insertShouldReturnProductDTOWhenAdminLoggedIn() throws Exception {

        String jsonBody = objectMapper.writeValueAsString(productDTO);

        ResultActions result = mockMvc
                .perform(post("/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print());
        result.andExpect(status().isCreated());
        result.andExpect(jsonPath("$.id").value(26L));
        result.andExpect(jsonPath("$.name").value("Console PlayStation 5"));
        result.andExpect(jsonPath("$.description").value("Jogue como nunca antes. Curta uma nova geração de jogos incríveis do PlayStation"));
        result.andExpect(jsonPath("$.price").value(4269.90));
        result.andExpect(jsonPath("$.imgUrl").value("playstation-5.png"));
        result.andExpect(jsonPath("$.categories[0].id").value(2L));
    }

    @Test
    public void insertShouldReturnUnprocessableEntityWhenAdminLoggedInAndNameIsInvalid() throws Exception {

        product.setName("ab");
        productDTO = new ProductDTO(product);

        String jsonBody = objectMapper.writeValueAsString(productDTO);

        ResultActions result = mockMvc
                .perform(post("/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void insertShouldReturnUnprocessableEntityWhenAdminLoggedInAndPriceIsNegative() throws Exception {

        product.setPrice(-50.0);
        productDTO = new ProductDTO(product);

        String jsonBody = objectMapper.writeValueAsString(productDTO);

        ResultActions result = mockMvc
                .perform(post("/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void insertShouldReturnUnprocessableEntityWhenAdminLoggedInAndPriceIsZero() throws Exception {

        product.setPrice(0.0);
        productDTO = new ProductDTO(product);

        String jsonBody = objectMapper.writeValueAsString(productDTO);

        ResultActions result = mockMvc
                .perform(post("/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void insertShouldReturnUnprocessableEntityWhenAdminLoggedInAndCategoryIsMissing()  throws Exception {

        product.getCategories().clear();
        productDTO = new ProductDTO(product);

        String jsonBody = objectMapper.writeValueAsString(productDTO);

        ResultActions result = mockMvc
                .perform(post("/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void insertShouldReturnForbiddenWhenClientLoggedIn() throws Exception {

        String jsonBody = objectMapper.writeValueAsString(productDTO);

        ResultActions result = mockMvc
                .perform(post("/products")
                        .header("Authorization", "Bearer " + clientToken)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isForbidden());
    }

    @Test
    public void insertShouldReturnUnauthorizedWhenTokenIsInvalid() throws Exception {

        String jsonBody = objectMapper.writeValueAsString(productDTO);

        ResultActions result = mockMvc
                .perform(post("/products")
                        .header("Authorization", "Bearer " + invalidToken)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void updateShouldReturnProductDTOWhenIdExistAndAdminLoggedIn() throws Exception {

        String jsonBody = objectMapper.writeValueAsString(productDTO);

        ResultActions result = mockMvc
                .perform(put("/products/{id}", existingId)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$.id").value(existingId));
        result.andExpect(jsonPath("$.name").value("Console PlayStation 5"));
        result.andExpect(jsonPath("$.description").value("Jogue como nunca antes. Curta uma nova geração de jogos incríveis do PlayStation"));
        result.andExpect(jsonPath("$.price").value(4269.90));
        result.andExpect(jsonPath("$.imgUrl").value("playstation-5.png"));
        result.andExpect(jsonPath("$.categories[0].id").value(2L));
    }

    @Test
    public void updateShouldThrowNotFoundWhenIdDoesNotExistAndAdminLoggedIn() throws Exception {

        String jsonBody = objectMapper.writeValueAsString(productDTO);

        ResultActions result = mockMvc
                .perform(put("/products/{id}", nonExistingId)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isNotFound());
    }

    @Test
    public void updateShouldReturnUnprocessableEntityWhenIdExistsAndAdminLoggedAndInvalidName() throws Exception {

        product.setName("ab");
        productDTO = new ProductDTO(product);

        String jsonBody = objectMapper.writeValueAsString(productDTO);

        ResultActions result = mockMvc
                .perform(put("/products/{id}", existingId)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));

        result.andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void updateShouldReturnUnprocessableEntityWhenIdExistsAndAdminLoggedInAndInvalidDescription() throws Exception {

        product.setDescription("ab");
        productDTO = new ProductDTO(product);

        String jsonBody = objectMapper.writeValueAsString(productDTO);

        ResultActions result = mockMvc
                .perform(put("/products/{id}", existingId)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));

        result.andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void updateShouldReturnUnprocessableEntityWhenIdExistsAndAdminLoggedInAndPriceIsNegative() throws Exception {

        product.setPrice(-2.0);
        productDTO = new ProductDTO(product);

        String jsonBody = objectMapper.writeValueAsString(productDTO);

        ResultActions result = mockMvc
                .perform(put("/products/{id}", existingId)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));

        result.andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void updateShouldReturnUnprocessableEntityWhenIdExistsAndAdminLoggedInAndPriceIsZero() throws Exception {

        product.setPrice(0.0);
        productDTO = new ProductDTO(product);

        String jsonBody = objectMapper.writeValueAsString(productDTO);

        ResultActions result = mockMvc
                .perform(put("/products/{id}", existingId)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));

        result.andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void updateShouldReturnUnprocessableEntityWhenIdExistsAndAdminLoggedInAndProductHasNoCategory() throws Exception {

        product.getCategories().clear();
        productDTO = new ProductDTO(product);

        String jsonBody = objectMapper.writeValueAsString(productDTO);

        ResultActions result = mockMvc
                .perform(put("/products/{id}", existingId)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));

        result.andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void updateShouldReturnForbiddenWhenIdExistsAndClientLogged() throws Exception {

        String jsonBody = objectMapper.writeValueAsString(productDTO);

        ResultActions result = mockMvc
                .perform(put("/products/{id}", existingId)
                        .header("Authorization", "Bearer " + clientToken)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));

        result.andExpect(status().isForbidden());
    }

    @Test
    public void updateShouldReturnUnauthorizedWhenTokenIsInvalid() throws Exception {

        String jsonBody = objectMapper.writeValueAsString(productDTO);

        ResultActions result = mockMvc
                .perform(put("/products/{id}", existingId)
                        .header("Authorization", "Bearer " + invalidToken)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));

        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void deleteShouldReturnNoContentWhenIdExistAndAdminLoggedIn() throws Exception {
        ResultActions result = mockMvc
                .perform(delete("/products/{id}", existingId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON));
        result.andExpect(status().isNoContent());
    }

    @Test
    public void deleteShouldReturnResourceNotFoundWhenIdDoesNotExistAndAdminLoggedIn() throws Exception {
        ResultActions result = mockMvc
                .perform(delete("/products/{id}", nonExistingId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON));
        result.andExpect(status().isNotFound());
    }

    @Test
    @Transactional(propagation = Propagation.SUPPORTS)
    public void deleteShouldReturnBadRequestWhenDependentIdAndAdminLoggedIn() throws Exception {
        ResultActions result = mockMvc
                .perform(delete("/products/{id}", dependentId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON));
        result.andExpect(status().isBadRequest());
    }

    @Test
    public void deleteShouldReturnForbiddenWhenIdExistAndClientLoggedIn() throws Exception {
        ResultActions result = mockMvc
                .perform(delete("/products/{id}", existingId)
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON));
        result.andExpect(status().isForbidden());
    }

    @Test
    public void deleteShouldReturnUnauthorizedWhenTokenIsInvalid() throws Exception {
        ResultActions result = mockMvc
                .perform(delete("/products/{id}", existingId)
                        .header("Authorization", "Bearer " + invalidToken)
                        .contentType(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnauthorized());
    }
    
}
