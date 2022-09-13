package com.jvbarbosa.dscommerce.services;

import com.jvbarbosa.dscommerce.dto.ProductDTO;
import com.jvbarbosa.dscommerce.entities.Product;
import com.jvbarbosa.dscommerce.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository repository;

    @Transactional(readOnly = true)
    public ProductDTO findById(Long id) {
        Product product = repository.findById(id).get();
        return new ProductDTO(product);
    }
}
