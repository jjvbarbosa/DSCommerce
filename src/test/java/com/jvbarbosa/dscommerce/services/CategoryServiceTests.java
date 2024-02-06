package com.jvbarbosa.dscommerce.services;

import com.jvbarbosa.dscommerce.dto.CategoryDTO;
import com.jvbarbosa.dscommerce.entities.Category;
import com.jvbarbosa.dscommerce.factories.CategoryFactory;
import com.jvbarbosa.dscommerce.repositories.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class CategoryServiceTests {

    @InjectMocks
    private CategoryService service;

    @Mock
    private CategoryRepository repository;

    private Category category;
    private List<Category> list;

    @BeforeEach
    void setUp() throws Exception {
        category = CategoryFactory.createCategory();

        list = new ArrayList<>();
        list.add(category);

        when(repository.findAll()).thenReturn(list);
    }

    @Test
    public void findAllShouldReturnListCategoryDTO() {
        List<CategoryDTO> result = service.findAll();

        assertEquals(result.size(), 1);
        assertEquals(result.getFirst().getId(), category.getId());
        assertEquals(result.getFirst().getName(), category.getName());
    }
}
