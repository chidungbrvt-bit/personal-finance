package com.dung.personal_finance_backend.repository;

import com.dung.personal_finance_backend.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByUserId(Long userId);
    List<Budget> findByUserIdAndYearAndMonth(Long userId, Integer year, Integer month);
    Optional<Budget> findByIdAndUserId(Long budgetId, Long userId);
    Boolean existsByUserIdAndCategoryIdAndYearAndMonth(
            Long userId,
            Long categoryId,
            Integer year,
            Integer month
    );
    Boolean existsByCategoryIdAndUserId(Long categoryId, Long userId);
    boolean existsByUserIdAndCategoryIdAndYearAndMonthAndIdNot(
            Long userId,
            Long categoryId,
            Integer year,
            Integer month,
            Long budgetId
    );
}
