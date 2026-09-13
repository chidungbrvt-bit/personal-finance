package com.dung.personal_finance_backend.repository;

import com.dung.personal_finance_backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUserId(Long userId);
    Optional<Category> findByIdAndUserId(Long categoryId, Long userId);
    List<Category> findByUserIdAndActiveTrue(Long userId);
}
