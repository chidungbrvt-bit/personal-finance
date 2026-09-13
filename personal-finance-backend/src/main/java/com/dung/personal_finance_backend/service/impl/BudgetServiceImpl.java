package com.dung.personal_finance_backend.service.impl;

import com.dung.personal_finance_backend.dto.BudgetDto;
import com.dung.personal_finance_backend.dto.BudgetProgressDto;
import com.dung.personal_finance_backend.entity.Budget;
import com.dung.personal_finance_backend.entity.Category;
import com.dung.personal_finance_backend.entity.FinancialTransaction;
import com.dung.personal_finance_backend.entity.User;
import com.dung.personal_finance_backend.enums.TransactionType;
import com.dung.personal_finance_backend.exception.BudgetNotFoundException;
import com.dung.personal_finance_backend.exception.BusinessRuleException;
import com.dung.personal_finance_backend.exception.CategoryNotFoundException;
import com.dung.personal_finance_backend.exception.UserNotFoundException;
import com.dung.personal_finance_backend.mapper.BudgetMapper;
import com.dung.personal_finance_backend.repository.BudgetRepository;
import com.dung.personal_finance_backend.repository.CategoryRepository;
import com.dung.personal_finance_backend.repository.FinancialTransactionRepository;
import com.dung.personal_finance_backend.repository.UserRepository;
import com.dung.personal_finance_backend.service.BudgetService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

@Service
public class BudgetServiceImpl implements BudgetService {
    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final FinancialTransactionRepository financialTransactionRepository;


    public BudgetServiceImpl(
            BudgetRepository budgetRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            FinancialTransactionRepository financialTransactionRepository
    ) {
        this.budgetRepository = budgetRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.financialTransactionRepository = financialTransactionRepository;
    }

    @Override
    @Transactional
    public BudgetDto createBudget(Long userId, BudgetDto budgetDto) {
        User user = getUser(userId);
        Category category = getExpenseCategory(userId, budgetDto.getCategoryId());
        validateBudgetValues(budgetDto);
        if (!Boolean.TRUE.equals(category.getActive())) {
            throw new BusinessRuleException(
                    "Cannot use an inactive category"
            );
        }
        Boolean duplicateExits = budgetRepository
                .existsByUserIdAndCategoryIdAndYearAndMonth(
                        userId,
                        budgetDto.getCategoryId(),
                        budgetDto.getYear(),
                        budgetDto.getMonth()
                );

        if (duplicateExits) {
            throw new BusinessRuleException(
                    "A budget already exists for this category, year, and month"
            );
        }

        Budget budget = BudgetMapper.toEntity(budgetDto);
        budget.setUser(user);
        budget.setCategory(category);

        Budget savedBudget = budgetRepository.save(budget);
        return BudgetMapper.toDto(savedBudget);
    }

    @Override
    public List<BudgetDto> getBudgetsByUserId(Long userId) {
        getUser(userId);

        List<Budget> budgets = budgetRepository.findByUserId(userId);
        List<BudgetDto> budgetDtos = new ArrayList<>();
        for (Budget budget : budgets) {
            budgetDtos.add(BudgetMapper.toDto(budget));
        }
        return budgetDtos;
    }

    @Override
    public List<BudgetDto> getBudgetsByMonth(
            Long userId,
            Integer year,
            Integer month
    ) {
        getUser(userId);
        validateYearAndMonth(year, month);

        List<Budget> budgets =
                budgetRepository.findByUserIdAndYearAndMonth(userId, year, month);
        List<BudgetDto> budgetDtos = new ArrayList<>();
        for (Budget budget : budgets) {
            budgetDtos.add(BudgetMapper.toDto(budget));
        }
        return budgetDtos;
    }

    @Override
    public BudgetDto getBudgetById(Long userId, Long budgetId) {
        getUser(userId);
        Budget budget = getBudget(userId, budgetId);
        return BudgetMapper.toDto(budget);
    }

    @Override
    public BudgetDto updateBudget(
            Long userId,
            Long budgetId,
            BudgetDto budgetDto
    ) {
        getUser(userId);
        Budget budget = getBudget(userId, budgetId);
        Category category = getExpenseCategory(userId, budgetDto.getCategoryId());
        validateBudgetValues(budgetDto);
        if (!Boolean.TRUE.equals(category.getActive())) {
            throw new BusinessRuleException(
                    "Cannot use an inactive category"
            );
        }
        Boolean duplicateExists = budgetRepository
                .existsByUserIdAndCategoryIdAndYearAndMonthAndIdNot(
                        userId,
                        category.getId(),
                        budgetDto.getYear(),
                        budgetDto.getMonth(),
                        budgetId
                );

        if (duplicateExists) {
            throw new BusinessRuleException(
                    "A budget already exists for this category, year, and month"
            );
        }

        budget.setYear(budgetDto.getYear());
        budget.setMonth(budgetDto.getMonth());
        budget.setAmount(budgetDto.getAmount());
        budget.setCategory(category);

        Budget updatedBudget = budgetRepository.save(budget);
        return BudgetMapper.toDto(updatedBudget);
    }

    @Override
    @Transactional
    public void deleteBudget(Long userId, Long budgetId) {
        getUser(userId);
        Budget budget = getBudget(userId, budgetId);
        budgetRepository.delete(budget);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with id: " + userId
                ));
    }

    private Budget getBudget(Long userId, Long budgetId) {
        return budgetRepository.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new BudgetNotFoundException(
                        "Budget not found with id: " + budgetId
                ));
    }

    private Category getExpenseCategory(Long userId, Long categoryId) {
        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new CategoryNotFoundException(
                        "Category not found with id: " + categoryId
                ));

        if (category.getType() != TransactionType.EXPENSE) {
            throw new BusinessRuleException(
                    "A budget can only be created for an expense category"
            );
        }
        return category;
    }

    private void validateBudgetValues(BudgetDto budgetDto) {
        validateYearAndMonth(budgetDto.getYear(), budgetDto.getMonth());

        if (budgetDto.getAmount() == null
                || budgetDto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Budget amount must be greater than zero");
        }
    }

    private void validateYearAndMonth(Integer year, Integer month) {
        if (year == null || year < 2026) {
            throw new BusinessRuleException("Budget year must be valid");
        }

        if (month == null || month < 1 || month > 12) {
            throw new BusinessRuleException("Budget month must be between 1 and 12");
        }
    }

    @Override
    public BudgetProgressDto getBudgetProgress(Long userId, Long budgetId) {
        getUser(userId);
        Budget budget = getBudget(userId, budgetId);
        LocalDate startDate = LocalDate.of(budget.getYear(), budget.getMonth(), 1);
        LocalDate endDate = startDate.plusMonths(1);
        List<FinancialTransaction> financialTransactions =
                financialTransactionRepository
                        .findByCategoryIdAndUserIdAndTransactionDateGreaterThanEqualAndTransactionDateLessThan(
                                budget.getCategory().getId(),
                                userId,
                                startDate,
                                endDate
                        );
        BigDecimal spentAmount = BigDecimal.ZERO;
        for (FinancialTransaction financialTransaction : financialTransactions) {
            if (financialTransaction.getType() == TransactionType.EXPENSE) {
                spentAmount = spentAmount.add(
                        financialTransaction.getAmount()
                );
            }
        }
        BigDecimal budgetAmount = budget.getAmount();
        if (budgetAmount == null
                || budgetAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException(
                    "Budget amount must be greater than 0"
            );
        }
        BigDecimal remainingAmount = budgetAmount.subtract(spentAmount);
        BigDecimal percentageUsed = spentAmount
                .multiply(BigDecimal.valueOf(100))
                .divide(budgetAmount, 2, RoundingMode.HALF_UP);
        Boolean overBudget = spentAmount.compareTo(budgetAmount) > 0;
        return new BudgetProgressDto(
                budgetId,
                budget.getCategory().getId(),
                budget.getCategory().getName(),
                budget.getYear(),
                budget.getMonth(),
                budgetAmount,
                spentAmount,
                remainingAmount,
                percentageUsed,
                overBudget
        );
    }
}