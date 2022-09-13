package com.jvbarbosa.dscommerce.repositories;

import com.jvbarbosa.dscommerce.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
