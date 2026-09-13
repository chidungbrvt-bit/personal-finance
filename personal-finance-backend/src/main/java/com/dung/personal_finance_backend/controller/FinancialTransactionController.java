package com.dung.personal_finance_backend.controller;

import com.dung.personal_finance_backend.dto.FinancialTransactionDto;
import com.dung.personal_finance_backend.enums.PeriodType;
import com.dung.personal_finance_backend.enums.TransactionType;
import com.dung.personal_finance_backend.exception.BusinessRuleException;
import com.dung.personal_finance_backend.response.ResponseHandler;
import com.dung.personal_finance_backend.service.FinancialTransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/transactions")
@PreAuthorize("@userAccess.canAccess(#userId, authentication)")
public class FinancialTransactionController {
    private final FinancialTransactionService financialTransactionService;

    public FinancialTransactionController(
            FinancialTransactionService financialTransactionService
    ) {
        this.financialTransactionService = financialTransactionService;
    }

    @PostMapping
    public ResponseEntity<Object> createFinancialTransaction(
            @PathVariable Long userId,
            @Valid @RequestBody FinancialTransactionDto financialTransactionDto
    ) {
        return ResponseHandler.responseBuilder(
                "Financial transaction created successfully",
                HttpStatus.CREATED,
                financialTransactionService.createFinancialTransaction(
                        userId,
                        financialTransactionDto
                )
        );
    }

    @GetMapping
    public ResponseEntity<Object> getFinancialTransactions(
            @PathVariable Long userId,
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) PeriodType period,
            @RequestParam(required = false) Integer day,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer quarter,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate
    ) {
        int filterCount = 0;
        if (accountId != null) {
            filterCount++;
        }
        if (categoryId != null) {
            filterCount++;
        }
        if (type != null) {
            filterCount++;
        }
        if (filterCount > 1) {
            throw new BusinessRuleException( "Use only one of accountId, categoryId, or type");
        }
        if (period == null &&
                (day != null
                        || month != null
                        || quarter != null
                        || year != null
                        || fromDate != null
                        || toDate != null)) {
            throw new BusinessRuleException(
                    "Period is required when using date filters"
            );
        }

        LocalDate startDate = null;
        LocalDate endDate = null;
        if (period == PeriodType.DAY) {
            if (year == null || month == null || day == null) {
                throw new BusinessRuleException(
                        "Year, month and day are required for DAY period"
                );
            }
            if (quarter != null || fromDate != null || toDate != null) {
                throw new BusinessRuleException(
                        "Quarter, fromDate and toDate is not allowed for DAY period"
                );
            }
            startDate = LocalDate.of(year, month, day);
            endDate = startDate.plusDays(1);
        }
        if (period == PeriodType.MONTH) {
            if (year == null || month == null) {
                throw new BusinessRuleException(
                        "Year and month are required for MONTH period"
                );
            }
            if (day != null || quarter != null || fromDate != null || toDate != null) {
                throw new BusinessRuleException(
                        "Day, quarter, fromDate and toDate are not allowed for MONTH period"
                );
            }
            if (month < 1 || month > 12) {
                throw new BusinessRuleException(
                        "Month must be between 1 and 12"
                );
            }
            startDate = LocalDate.of(year, month, 1);
            endDate = startDate.plusMonths(1);
        }
        if (period == PeriodType.QUARTER) {
            if (year == null || quarter == null) {
                throw new BusinessRuleException(
                        "Year and quarter are required for QUARTER period"
                );
            }
            if (day != null || month != null || fromDate != null || toDate != null) {
                throw new BusinessRuleException(
                        "Day, month, fromDate and toDate are not allowed for QUARTER period"
                );
            }
            if (quarter < 1 || quarter > 4) {
                throw new BusinessRuleException(
                        "Quarter must be between 1 and 4"
                );
            }
            int startMonth = (quarter - 1) * 3 + 1;
            startDate = LocalDate.of(year, startMonth, 1);
            endDate = startDate.plusMonths(3);
        }
        if (period == PeriodType.YEAR) {
            if (year == null) {
                throw new BusinessRuleException(
                        "Year is required for YEAR period"
                );
            }
            if (day != null || month != null || quarter != null || fromDate != null || toDate != null) {
                throw new BusinessRuleException(
                        "Day, month, quarter, fromDate and toDate are not allowed for YEAR period"
                );
            }
            startDate = LocalDate.of(year, 1, 1);
            endDate = startDate.plusYears(1);
        }
        if (period == PeriodType.CUSTOM) {
            if (fromDate == null || toDate == null) {
                throw new BusinessRuleException(
                        "From date and to date are required for RANGE period"
                );
            }
            if (day != null || month != null || quarter != null || year != null) {
                throw new BusinessRuleException(
                        "Day, month, quarter and year are not allowed for RANGE period"
                );
            }
            if (fromDate.isAfter(toDate)) {
                throw new BusinessRuleException(
                        "From date must not be after to date"
                );
            }
            startDate = fromDate;
            endDate = toDate.plusDays(1);
        }

        List<FinancialTransactionDto> data;
        if (period != null) {
            if (accountId != null) {
                data = financialTransactionService
                        .getFinancialTransactionsByAccountAndDateRange(
                                userId,
                                accountId,
                                startDate,
                                endDate
                        );
            } else if (categoryId != null) {
                data = financialTransactionService
                        .getFinancialTransactionsByCategoryAndDateRange(
                                userId,
                                categoryId,
                                startDate,
                                endDate
                        );
            } else if (type != null) {
                data = financialTransactionService
                        .getFinancialTransactionsByTypeAndDateRange(
                                userId,
                                type,
                                startDate,
                                endDate
                        );
            } else {
                data = financialTransactionService
                        .getFinancialTransactionsByDateRange(
                                userId,
                                startDate,
                                endDate
                        );
            }
        } else {
            if (accountId != null) {
                data = financialTransactionService
                        .getFinancialTransactionsByAccountId(
                                userId,
                                accountId
                        );
            } else if (categoryId != null) {
                data = financialTransactionService
                        .getFinancialTransactionsByCategoryId(
                                userId,
                                categoryId
                        );
            } else if (type != null) {
                data = financialTransactionService
                        .getFinancialTransactionsByType(
                                userId,
                                type
                        );
            } else {
                data = financialTransactionService
                        .getFinancialTransactionsByUserId(userId);
            }
        }

        return ResponseHandler.responseBuilder(
                "Financial transactions retrieved successfully",
                HttpStatus.OK,
                data
        );
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<Object> getFinancialTransactionById(
            @PathVariable Long userId,
            @PathVariable Long transactionId
    ) {
        return ResponseHandler.responseBuilder(
                "Financial transaction retrieved successfully",
                HttpStatus.OK,
                financialTransactionService.getFinancialTransactionById(
                        userId,
                        transactionId
                )
        );
    }

    @PutMapping("/{transactionId}")
    public ResponseEntity<Object> updateFinancialTransaction(
            @PathVariable Long userId,
            @PathVariable Long transactionId,
            @Valid @RequestBody FinancialTransactionDto financialTransactionDto
    ) {
        return ResponseHandler.responseBuilder(
                "Financial transaction updated successfully",
                HttpStatus.OK,
                financialTransactionService.updateFinancialTransaction(
                        userId,
                        transactionId,
                        financialTransactionDto
                )
        );
    }

    @DeleteMapping("/{transactionId}")
    public ResponseEntity<Object> deleteFinancialTransaction(
            @PathVariable Long userId,
            @PathVariable Long transactionId
    ) {
        financialTransactionService.deleteFinancialTransaction(userId, transactionId);
        return ResponseHandler.responseBuilder(
                "Financial transaction deleted successfully",
                HttpStatus.OK,
                null
        );
    }
}
