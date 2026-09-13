package com.dung.personal_finance_backend.mapper;

import com.dung.personal_finance_backend.dto.BudgetDto;
import com.dung.personal_finance_backend.entity.Budget;

public class BudgetMapper {

    public static BudgetDto toDto(Budget budget) {
        return new BudgetDto(
                budget.getId(),
                budget.getYear(),
                budget.getMonth(),
                budget.getAmount(),
                budget.getCategory().getId(),
                budget.getCreatedAt()
        );
    }

    public static Budget toEntity(BudgetDto budgetDto) {
        return new Budget(
                null,
                budgetDto.getYear(),
                budgetDto.getMonth(),
                budgetDto.getAmount(),
                null,
                null,
                null,
                null
        );
    }
}
