package com.jvbarbosa.dscommerce.repositories;

import com.jvbarbosa.dscommerce.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);
}
