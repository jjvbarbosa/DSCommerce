package com.jvbarbosa.dscommerce.repositories;

import com.jvbarbosa.dscommerce.entities.OrderItem;
import com.jvbarbosa.dscommerce.entities.OrderItemPK;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, OrderItemPK> {

}
