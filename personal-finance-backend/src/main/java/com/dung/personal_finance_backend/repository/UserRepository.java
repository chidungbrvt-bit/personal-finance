package com.dung.personal_finance_backend.repository;

import com.dung.personal_finance_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Boolean existsByEmailIgnoreCase(String email);
    Boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
    Optional<User> findByEmailIgnoreCase(String email);
}
