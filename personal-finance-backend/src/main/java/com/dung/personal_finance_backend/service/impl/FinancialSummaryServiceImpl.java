package com.dung.personal_finance_backend.service.impl;

import com.dung.personal_finance_backend.dto.FinancialSummaryDto;
import com.dung.personal_finance_backend.dto.FinancialSummaryRequestDto;
import com.dung.personal_finance_backend.entity.FinancialTransaction;
import com.dung.personal_finance_backend.enums.TransactionType;
import com.dung.personal_finance_backend.exception.BusinessRuleException;
import com.dung.personal_finance_backend.exception.UserNotFoundException;
import com.dung.personal_finance_backend.repository.FinancialTransactionRepository;
import com.dung.personal_finance_backend.repository.UserRepository;
import com.dung.personal_finance_backend.service.FinancialSummaryService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FinancialSummaryServiceImpl implements FinancialSummaryService {
    private final UserRepository userRepository;
    private final FinancialTransactionRepository financialTransactionRepository;
    public FinancialSummaryServiceImpl(
            UserRepository userRepository,
            FinancialTransactionRepository financialTransactionRepository
    ) {
        this.userRepository = userRepository;
        this.financialTransactionRepository = financialTransactionRepository;
    }

    @Override
    public FinancialSummaryDto getSummary(Long userId, FinancialSummaryRequestDto request) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        if (request == null || request.getPeriod() == null) {
            throw new BusinessRuleException("Period is required");
        }
        LocalDate startDate;
        LocalDate endDate;
        switch (request.getPeriod()) {
            case DAY -> {
                if (request.getYear() == null
                        || request.getMonth() == null
                        || request.getDay() == null) {
                    throw new BusinessRuleException("Year, month and day are required for DAY period");
                }
                if (request.getMonth() < 1 || request.getMonth() > 12) {
                    throw new BusinessRuleException(
                            "Month must be between 1 and 12"
                    );
                }
                startDate = LocalDate.of(request.getYear(), request.getMonth(), request.getDay());
                endDate = startDate.plusDays(1);
            }
            case MONTH -> {
                if (request.getYear() == null
                        || request.getMonth() == null) {
                    throw new BusinessRuleException(
                            "Year and month are required for MONTH period"
                    );
                }
                if (request.getMonth() < 1 || request.getMonth() > 12) {
                    throw new BusinessRuleException(
                            "Month must be between 1 and 12"
                    );
                }
                startDate = LocalDate.of(request.getYear(), request.getMonth(), 1);
                endDate = startDate.plusMonths(1);
            }
            case QUARTER -> {
                if (request.getYear() == null
                        || request.getQuarter() == null) {
                    throw new BusinessRuleException(
                            "Year and quarter are required for QUARTER period"
                    );
                }
                if (request.getQuarter() < 1
                        || request.getQuarter() > 4) {
                    throw new BusinessRuleException(
                            "Quarter must be between 1 and 4"
                    );
                }
                int startMonth = (request.getQuarter() - 1) * 3 + 1;
                startDate = LocalDate.of(request.getYear(), startMonth, 1);
                endDate = startDate.plusMonths(3);
            }
            case YEAR -> {
                if (request.getYear() == null) {
                    throw new BusinessRuleException(
                            "Year is required for YEAR period"
                    );
                }
                startDate = LocalDate.of(request.getYear(), 1, 1);
                endDate = startDate.plusYears(1);
            }
            case CUSTOM -> {
                if (request.getFromDate() == null
                        || request.getToDate() == null) {
                    throw new BusinessRuleException(
                            "From date and to date are required for CUSTOM period"
                    );
                }
                if (request.getFromDate().isAfter(request.getToDate())) {
                    throw new BusinessRuleException(
                            "From date must not be after to date"
                    );
                }
                startDate = request.getFromDate();
                endDate = request.getToDate().plusDays(1);
            }
            default -> throw new BusinessRuleException(
                    "Unsupported period type"
            );
        }
        List<FinancialTransaction> financialTransactions = financialTransactionRepository
                .findByUserIdAndTransactionDateGreaterThanEqualAndTransactionDateLessThan(
                        userId,
                        startDate,
                        endDate
                );
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;
        Map<String, BigDecimal> incomeByCategory = new HashMap<>();
        Map<String, BigDecimal> expensesByCategory = new HashMap<>();
        for (FinancialTransaction financialTransaction : financialTransactions) {
            if (financialTransaction.getType() == TransactionType.INCOME) {
                totalIncome = totalIncome.add(financialTransaction.getAmount());
                String categoryName = financialTransaction.getCategory().getName();
                BigDecimal currentAmount = incomeByCategory
                        .getOrDefault(categoryName, BigDecimal.ZERO);
                BigDecimal newAmount = currentAmount.add(financialTransaction.getAmount());
                incomeByCategory.put(categoryName, newAmount);
            } else {
                totalExpenses = totalExpenses.add(financialTransaction.getAmount());
                String categoryName = financialTransaction.getCategory().getName();
                BigDecimal currentAmount = expensesByCategory
                        .getOrDefault(categoryName, BigDecimal.ZERO);
                BigDecimal newAmount = currentAmount.add(financialTransaction.getAmount());
                expensesByCategory.put(categoryName, newAmount);
            }
        }
        BigDecimal netAmount = totalIncome.subtract(totalExpenses);
        return new FinancialSummaryDto(
                request.getPeriod(),
                startDate,
                endDate.minusDays(1),
                totalIncome,
                totalExpenses,
                netAmount,
                incomeByCategory,
                expensesByCategory
        );
    }
}
