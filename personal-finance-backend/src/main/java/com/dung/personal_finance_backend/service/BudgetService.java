package com.dung.personal_finance_backend.service;

import com.dung.personal_finance_backend.dto.BudgetDto;
import com.dung.personal_finance_backend.dto.BudgetProgressDto;

import java.util.List;

public interface BudgetService {
    BudgetDto createBudget(Long userId, BudgetDto budgetDto);
    List<BudgetDto> getBudgetsByUserId(Long userId);
    List<BudgetDto> getBudgetsByMonth(Long userId, Integer year, Integer month);
    BudgetDto getBudgetById(Long userId, Long budgetId);
    BudgetDto updateBudget(Long userId, Long budgetId, BudgetDto budgetDto);
    void deleteBudget(Long userId, Long budgetId);
    BudgetProgressDto getBudgetProgress(Long userId, Long budgetId);
}
