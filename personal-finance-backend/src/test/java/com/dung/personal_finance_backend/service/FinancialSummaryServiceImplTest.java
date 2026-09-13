package com.dung.personal_finance_backend.service;

import com.dung.personal_finance_backend.dto.FinancialSummaryDto;
import com.dung.personal_finance_backend.dto.FinancialSummaryRequestDto;
import com.dung.personal_finance_backend.entity.Category;
import com.dung.personal_finance_backend.entity.FinancialTransaction;
import com.dung.personal_finance_backend.entity.User;
import com.dung.personal_finance_backend.enums.PeriodType;
import com.dung.personal_finance_backend.enums.TransactionType;
import com.dung.personal_finance_backend.exception.BusinessRuleException;
import com.dung.personal_finance_backend.exception.UserNotFoundException;
import com.dung.personal_finance_backend.repository.FinancialTransactionRepository;
import com.dung.personal_finance_backend.repository.UserRepository;
import com.dung.personal_finance_backend.service.impl.FinancialSummaryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinancialSummaryServiceImplTest {
    private static final Long USER_ID = 1L;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FinancialTransactionRepository financialTransactionRepository;

    private FinancialSummaryServiceImpl financialSummaryService;

    @BeforeEach
    void setUp() {
        financialSummaryService = new FinancialSummaryServiceImpl(
                userRepository,
                financialTransactionRepository
        );
    }

    @Test
    void getSummary_shouldCalculateMonthlyTotalsAndGroupAmountsByCategory() {
        FinancialSummaryRequestDto request = createRequest(PeriodType.MONTH);
        request.setYear(2026);
        request.setMonth(9);

        List<FinancialTransaction> transactions = List.of(
                createTransaction(TransactionType.INCOME, "3000000", "Salary"),
                createTransaction(TransactionType.EXPENSE, "600000", "Food"),
                createTransaction(TransactionType.EXPENSE, "400000", "Food"),
                createTransaction(TransactionType.EXPENSE, "200000", "Transport")
        );

        mockExistingUser();
        when(financialTransactionRepository
                .findByUserIdAndTransactionDateGreaterThanEqualAndTransactionDateLessThan(
                        USER_ID,
                        LocalDate.of(2026, 9, 1),
                        LocalDate.of(2026, 10, 1)
                )).thenReturn(transactions);

        FinancialSummaryDto result = financialSummaryService.getSummary(USER_ID, request);

        assertEquals(new BigDecimal("3000000"), result.getTotalIncome());
        assertEquals(new BigDecimal("1200000"), result.getTotalExpenses());
        assertEquals(new BigDecimal("1800000"), result.getNetAmount());
        assertEquals(new BigDecimal("3000000"), result.getIncomeByCategory().get("Salary"));
        assertEquals(new BigDecimal("1000000"), result.getExpensesByCategory().get("Food"));
        assertEquals(new BigDecimal("200000"), result.getExpensesByCategory().get("Transport"));
    }

    @Test
    void getSummary_shouldCalculateDayStartAndEndDates() {
        FinancialSummaryRequestDto request = createRequest(PeriodType.DAY);
        request.setYear(2026);
        request.setMonth(9);
        request.setDay(6);

        mockEmptyTransactions(
                LocalDate.of(2026, 9, 6),
                LocalDate.of(2026, 9, 7)
        );

        FinancialSummaryDto result = financialSummaryService.getSummary(USER_ID, request);

        assertEquals(LocalDate.of(2026, 9, 6), result.getStartDate());
        assertEquals(LocalDate.of(2026, 9, 6), result.getEndDate());
    }

    @Test
    void getSummary_shouldCalculateMonthStartAndEndDates() {
        FinancialSummaryRequestDto request = createRequest(PeriodType.MONTH);
        request.setYear(2026);
        request.setMonth(9);

        mockEmptyTransactions(
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 10, 1)
        );

        FinancialSummaryDto result = financialSummaryService.getSummary(USER_ID, request);

        assertEquals(LocalDate.of(2026, 9, 1), result.getStartDate());
        assertEquals(LocalDate.of(2026, 9, 30), result.getEndDate());
    }

    @Test
    void getSummary_shouldUseExclusiveQueryEndForMonthBoundary() {
        FinancialSummaryRequestDto request = createRequest(PeriodType.MONTH);
        request.setYear(2026);
        request.setMonth(9);

        mockEmptyTransactions(
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 10, 1)
        );

        financialSummaryService.getSummary(USER_ID, request);

        verify(financialTransactionRepository)
                .findByUserIdAndTransactionDateGreaterThanEqualAndTransactionDateLessThan(
                        USER_ID,
                        LocalDate.of(2026, 9, 1),
                        LocalDate.of(2026, 10, 1)
                );
    }

    @Test
    void getSummary_shouldCalculateQuarterStartAndEndDates() {
        FinancialSummaryRequestDto request = createRequest(PeriodType.QUARTER);
        request.setYear(2026);
        request.setQuarter(3);

        mockEmptyTransactions(
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 10, 1)
        );

        FinancialSummaryDto result = financialSummaryService.getSummary(USER_ID, request);

        assertEquals(LocalDate.of(2026, 7, 1), result.getStartDate());
        assertEquals(LocalDate.of(2026, 9, 30), result.getEndDate());
    }

    @Test
    void getSummary_shouldCalculateYearStartAndEndDates() {
        FinancialSummaryRequestDto request = createRequest(PeriodType.YEAR);
        request.setYear(2026);

        mockEmptyTransactions(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2027, 1, 1)
        );

        FinancialSummaryDto result = financialSummaryService.getSummary(USER_ID, request);

        assertEquals(LocalDate.of(2026, 1, 1), result.getStartDate());
        assertEquals(LocalDate.of(2026, 12, 31), result.getEndDate());
    }

    @Test
    void getSummary_shouldCalculateCustomStartAndEndDates() {
        FinancialSummaryRequestDto request = createRequest(PeriodType.CUSTOM);
        request.setFromDate(LocalDate.of(2026, 9, 5));
        request.setToDate(LocalDate.of(2026, 9, 20));

        mockEmptyTransactions(
                LocalDate.of(2026, 9, 5),
                LocalDate.of(2026, 9, 21)
        );

        FinancialSummaryDto result = financialSummaryService.getSummary(USER_ID, request);

        assertEquals(LocalDate.of(2026, 9, 5), result.getStartDate());
        assertEquals(LocalDate.of(2026, 9, 20), result.getEndDate());
    }

    @Test
    void getSummary_shouldRejectNullPeriod() {
        FinancialSummaryRequestDto request = createRequest(null);

        mockExistingUser();

        assertThrows(
                BusinessRuleException.class,
                () -> financialSummaryService.getSummary(USER_ID, request)
        );
    }

    @Test
    void getSummary_shouldRejectCustomRangeWhenFromDateIsAfterToDate() {
        FinancialSummaryRequestDto request = createRequest(PeriodType.CUSTOM);
        request.setFromDate(LocalDate.of(2026, 9, 20));
        request.setToDate(LocalDate.of(2026, 9, 5));

        mockExistingUser();

        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> financialSummaryService.getSummary(USER_ID, request)
        );

        assertEquals("From date must not be after to date", exception.getMessage());
    }

    @Test
    void getSummary_shouldReturnZeroTotalsWhenThereAreNoTransactions() {
        FinancialSummaryRequestDto request = createRequest(PeriodType.MONTH);
        request.setYear(2026);
        request.setMonth(9);

        mockEmptyTransactions(
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 10, 1)
        );

        FinancialSummaryDto result = financialSummaryService.getSummary(USER_ID, request);

        assertEquals(BigDecimal.ZERO, result.getTotalIncome());
        assertEquals(BigDecimal.ZERO, result.getTotalExpenses());
        assertEquals(BigDecimal.ZERO, result.getNetAmount());
        assertEquals(0, result.getIncomeByCategory().size());
        assertEquals(0, result.getExpensesByCategory().size());
    }

    @Test
    void getSummary_shouldThrowWhenUserDoesNotExist() {
        FinancialSummaryRequestDto request = createRequest(PeriodType.MONTH);
        request.setYear(2026);
        request.setMonth(9);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> financialSummaryService.getSummary(USER_ID, request)
        );

        assertEquals("User not found with id: 1", exception.getMessage());
    }

    private FinancialSummaryRequestDto createRequest(PeriodType period) {
        FinancialSummaryRequestDto request = new FinancialSummaryRequestDto();
        request.setPeriod(period);
        return request;
    }

    private void mockExistingUser() {
        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(new User()));
    }

    private void mockEmptyTransactions(LocalDate startDate, LocalDate endDate) {
        mockExistingUser();
        when(financialTransactionRepository
                .findByUserIdAndTransactionDateGreaterThanEqualAndTransactionDateLessThan(
                        USER_ID,
                        startDate,
                        endDate
                )).thenReturn(List.of());
    }

    private FinancialTransaction createTransaction(
            TransactionType type,
            String amount,
            String categoryName
    ) {
        Category category = new Category();
        category.setName(categoryName);

        FinancialTransaction transaction = new FinancialTransaction();
        transaction.setType(type);
        transaction.setAmount(new BigDecimal(amount));
        transaction.setCategory(category);
        return transaction;
    }
}
