package com.jvbarbosa.dscommerce.repositories;

import com.jvbarbosa.dscommerce.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

}
