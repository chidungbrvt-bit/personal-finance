package com.dung.personal_finance_backend.service;

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
import com.dung.personal_finance_backend.repository.BudgetRepository;
import com.dung.personal_finance_backend.repository.CategoryRepository;
import com.dung.personal_finance_backend.repository.FinancialTransactionRepository;
import com.dung.personal_finance_backend.repository.UserRepository;
import com.dung.personal_finance_backend.service.impl.BudgetServiceImpl;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetServiceImplTest {
    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private FinancialTransactionRepository financialTransactionRepository;

    private BudgetServiceImpl budgetService;

    @BeforeEach
    void setUp() {
        budgetService = new BudgetServiceImpl(
                budgetRepository,
                userRepository,
                categoryRepository,
                financialTransactionRepository
        );
    }

    @Test
    void createBudget_shouldCreateBudgetSuccessfully() {
        Long userId = 1L;
        Long categoryId = 20L;
        User user = createUser(userId);
        Category category = createCategory(categoryId, user, TransactionType.EXPENSE, true);
        BudgetDto request = createBudgetDto(2026, 9, "1000000", categoryId);

        stubUserAndCategory(userId, user, categoryId, category);
        when(budgetRepository.existsByUserIdAndCategoryIdAndYearAndMonth(
                userId, categoryId, 2026, 9
        )).thenReturn(false);
        when(budgetRepository.save(any(Budget.class))).thenAnswer(invocation -> {
            Budget budget = invocation.getArgument(0);
            budget.setId(100L);
            return budget;
        });

        BudgetDto result = budgetService.createBudget(userId, request);

        assertEquals(100L, result.getId());
        assertEquals(new BigDecimal("1000000"), result.getAmount());
        assertEquals(categoryId, result.getCategoryId());
        verify(budgetRepository).save(any(Budget.class));
    }

    @Test
    void createBudget_shouldRejectMissingCategory() {
        Long userId = 1L;
        Long categoryId = 999L;
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(createUser(userId)));
        when(categoryRepository.findByIdAndUserId(categoryId, userId))
                .thenReturn(Optional.empty());

        assertThrows(
                CategoryNotFoundException.class,
                () -> budgetService.createBudget(
                        userId, createBudgetDto(2026, 9, "1000000", categoryId)
                )
        );
        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    void createBudget_shouldRejectIncomeCategory() {
        Long userId = 1L;
        User user = createUser(userId);
        Category category = createCategory(20L, user, TransactionType.INCOME, true);
        stubUserAndCategory(userId, user, 20L, category);

        assertThrows(
                BusinessRuleException.class,
                () -> budgetService.createBudget(
                        userId, createBudgetDto(2026, 9, "1000000", 20L)
                )
        );
        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    void createBudget_shouldRejectInactiveCategory() {
        Long userId = 1L;
        User user = createUser(userId);
        Category category = createCategory(20L, user, TransactionType.EXPENSE, false);
        stubUserAndCategory(userId, user, 20L, category);

        assertThrows(
                BusinessRuleException.class,
                () -> budgetService.createBudget(
                        userId, createBudgetDto(2026, 9, "1000000", 20L)
                )
        );
        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    void createBudget_shouldRejectDuplicateBudget() {
        Long userId = 1L;
        User user = createUser(userId);
        Category category = createCategory(20L, user, TransactionType.EXPENSE, true);
        stubUserAndCategory(userId, user, 20L, category);
        when(budgetRepository.existsByUserIdAndCategoryIdAndYearAndMonth(
                userId, 20L, 2026, 9
        )).thenReturn(true);

        assertThrows(
                BusinessRuleException.class,
                () -> budgetService.createBudget(
                        userId, createBudgetDto(2026, 9, "1000000", 20L)
                )
        );
        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    void createBudget_shouldRejectZeroAmount() {
        assertCreateRejectsInvalidValues(createBudgetDto(2026, 9, "0", 20L));
    }

    @Test
    void createBudget_shouldRejectNegativeAmount() {
        assertCreateRejectsInvalidValues(createBudgetDto(2026, 9, "-1", 20L));
    }

    @Test
    void createBudget_shouldRejectNullAmount() {
        assertCreateRejectsInvalidValues(createBudgetDto(2026, 9, null, 20L));
    }

    @Test
    void createBudget_shouldRejectInvalidYear() {
        assertCreateRejectsInvalidValues(createBudgetDto(0, 9, "1000", 20L));
    }

    @Test
    void createBudget_shouldRejectMonthBelowOne() {
        assertCreateRejectsInvalidValues(createBudgetDto(2026, 0, "1000", 20L));
    }

    @Test
    void createBudget_shouldRejectMonthAboveTwelve() {
        assertCreateRejectsInvalidValues(createBudgetDto(2026, 13, "1000", 20L));
    }

    @Test
    void getBudgetsByUserId_shouldReturnAllUserBudgets() {
        Long userId = 1L;
        User user = createUser(userId);
        Category category = createCategory(20L, user, TransactionType.EXPENSE, true);
        Budget budget = createBudget(100L, user, category, 2026, 9, "1000000");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(budgetRepository.findByUserId(userId)).thenReturn(List.of(budget));

        List<BudgetDto> result = budgetService.getBudgetsByUserId(userId);

        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getId());
        assertEquals(20L, result.get(0).getCategoryId());
    }

    @Test
    void getBudgetsByMonth_shouldReturnMatchingBudgets() {
        Long userId = 1L;
        User user = createUser(userId);
        Category category = createCategory(20L, user, TransactionType.EXPENSE, true);
        Budget budget = createBudget(100L, user, category, 2026, 9, "1000000");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(budgetRepository.findByUserIdAndYearAndMonth(userId, 2026, 9))
                .thenReturn(List.of(budget));

        List<BudgetDto> result = budgetService.getBudgetsByMonth(userId, 2026, 9);

        assertEquals(1, result.size());
        assertEquals(2026, result.get(0).getYear());
        assertEquals(9, result.get(0).getMonth());
        verify(budgetRepository).findByUserIdAndYearAndMonth(userId, 2026, 9);
    }

    @Test
    void getBudgetById_shouldReturnBudgetSuccessfully() {
        Long userId = 1L;
        Long budgetId = 100L;
        User user = createUser(userId);
        Category category = createCategory(20L, user, TransactionType.EXPENSE, true);
        Budget budget = createBudget(budgetId, user, category, 2026, 9, "1000000");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(budgetRepository.findByIdAndUserId(budgetId, userId))
                .thenReturn(Optional.of(budget));

        BudgetDto result = budgetService.getBudgetById(userId, budgetId);

        assertEquals(budgetId, result.getId());
        assertEquals(new BigDecimal("1000000"), result.getAmount());
    }

    @Test
    void getBudgetById_shouldRejectMissingBudget() {
        Long userId = 1L;
        Long budgetId = 999L;
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(createUser(userId)));
        when(budgetRepository.findByIdAndUserId(budgetId, userId))
                .thenReturn(Optional.empty());

        assertThrows(
                BudgetNotFoundException.class,
                () -> budgetService.getBudgetById(userId, budgetId)
        );
    }

    @Test
    void updateBudget_shouldMutateExistingBudgetAndSaveIt() {
        Long userId = 1L;
        Long budgetId = 100L;
        User user = createUser(userId);
        Category oldCategory = createCategory(10L, user, TransactionType.EXPENSE, true);
        Category newCategory = createCategory(20L, user, TransactionType.EXPENSE, true);
        Budget budget = createBudget(budgetId, user, oldCategory, 2026, 8, "500000");
        BudgetDto request = createBudgetDto(2026, 9, "1000000", 20L);

        stubUpdateLookups(userId, budgetId, user, budget, newCategory);
        when(budgetRepository.existsByUserIdAndCategoryIdAndYearAndMonthAndIdNot(
                userId, 20L, 2026, 9, budgetId
        )).thenReturn(false);
        when(budgetRepository.save(budget)).thenReturn(budget);

        BudgetDto result = budgetService.updateBudget(userId, budgetId, request);

        assertEquals(2026, budget.getYear());
        assertEquals(9, budget.getMonth());
        assertEquals(new BigDecimal("1000000"), budget.getAmount());
        assertSame(newCategory, budget.getCategory());
        assertEquals(budgetId, result.getId());
        verify(budgetRepository).save(budget);
    }

    @Test
    void updateBudget_shouldRejectDuplicateWithAnotherBudget() {
        BudgetUpdateFixture fixture = createBudgetUpdateFixture();
        stubUpdateLookups(
                fixture.userId, fixture.budgetId, fixture.user,
                fixture.budget, fixture.category
        );
        when(budgetRepository.existsByUserIdAndCategoryIdAndYearAndMonthAndIdNot(
                fixture.userId, fixture.category.getId(), 2026, 9, fixture.budgetId
        )).thenReturn(true);

        assertThrows(
                BusinessRuleException.class,
                () -> budgetService.updateBudget(
                        fixture.userId, fixture.budgetId, fixture.request
                )
        );
        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    void updateBudget_shouldAllowKeepingItsCurrentUniqueCombination() {
        BudgetUpdateFixture fixture = createBudgetUpdateFixture();
        stubUpdateLookups(
                fixture.userId, fixture.budgetId, fixture.user,
                fixture.budget, fixture.category
        );
        when(budgetRepository.existsByUserIdAndCategoryIdAndYearAndMonthAndIdNot(
                fixture.userId, fixture.category.getId(), 2026, 9, fixture.budgetId
        )).thenReturn(false);
        when(budgetRepository.save(fixture.budget)).thenReturn(fixture.budget);

        BudgetDto result = budgetService.updateBudget(
                fixture.userId, fixture.budgetId, fixture.request
        );

        assertEquals(fixture.budgetId, result.getId());
        verify(budgetRepository).save(fixture.budget);
    }

    @Test
    void updateBudget_shouldRejectIncomeCategory() {
        assertUpdateRejectsCategory(TransactionType.INCOME, true);
    }

    @Test
    void updateBudget_shouldRejectInactiveCategory() {
        assertUpdateRejectsCategory(TransactionType.EXPENSE, false);
    }

    @Test
    void updateBudget_shouldRejectZeroAmount() {
        assertUpdateRejectsInvalidValues(createBudgetDto(2026, 9, "0", 20L));
    }

    @Test
    void updateBudget_shouldRejectInvalidYear() {
        assertUpdateRejectsInvalidValues(createBudgetDto(0, 9, "1000", 20L));
    }

    @Test
    void updateBudget_shouldRejectInvalidMonth() {
        assertUpdateRejectsInvalidValues(createBudgetDto(2026, 13, "1000", 20L));
    }

    @Test
    void updateBudget_shouldRejectMissingBudget() {
        Long userId = 1L;
        Long budgetId = 999L;
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(createUser(userId)));
        when(budgetRepository.findByIdAndUserId(budgetId, userId))
                .thenReturn(Optional.empty());

        assertThrows(
                BudgetNotFoundException.class,
                () -> budgetService.updateBudget(
                        userId, budgetId,
                        createBudgetDto(2026, 9, "1000", 20L)
                )
        );
        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    void deleteBudget_shouldDeleteExistingBudget() {
        Long userId = 1L;
        Long budgetId = 100L;
        User user = createUser(userId);
        Budget budget = createBudget(
                budgetId, user,
                createCategory(20L, user, TransactionType.EXPENSE, true),
                2026, 9, "1000"
        );
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(budgetRepository.findByIdAndUserId(budgetId, userId))
                .thenReturn(Optional.of(budget));

        budgetService.deleteBudget(userId, budgetId);

        verify(budgetRepository).delete(budget);
    }

    @Test
    void deleteBudget_shouldRejectMissingBudget() {
        Long userId = 1L;
        Long budgetId = 999L;
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(createUser(userId)));
        when(budgetRepository.findByIdAndUserId(budgetId, userId))
                .thenReturn(Optional.empty());

        assertThrows(
                BudgetNotFoundException.class,
                () -> budgetService.deleteBudget(userId, budgetId)
        );
        verify(budgetRepository, never()).delete(any(Budget.class));
    }

    @Test
    void getBudgetProgress_shouldReturnZeroSpentWhenThereAreNoTransactions() {
        ProgressFixture fixture = arrangeProgress(List.of());

        BudgetProgressDto result = budgetService.getBudgetProgress(
                fixture.userId, fixture.budgetId
        );

        assertEquals(BigDecimal.ZERO, result.getSpentAmount());
        assertEquals(new BigDecimal("1000000"), result.getRemainingAmount());
        assertEquals(new BigDecimal("0.00"), result.getPercentageUsed());
        assertFalse(result.getOverBudget());
    }

    @Test
    void getBudgetProgress_shouldCalculateSpendingUnderBudget() {
        ProgressFixture fixture = createProgressFixture();
        FinancialTransaction expense = createTransaction(
                TransactionType.EXPENSE, "600000", LocalDate.of(2026, 9, 10)
        );
        stubProgress(fixture, List.of(expense));

        BudgetProgressDto result = budgetService.getBudgetProgress(
                fixture.userId, fixture.budgetId
        );

        assertEquals(new BigDecimal("600000"), result.getSpentAmount());
        assertEquals(new BigDecimal("400000"), result.getRemainingAmount());
        assertEquals(new BigDecimal("60.00"), result.getPercentageUsed());
        assertFalse(result.getOverBudget());
    }

    @Test
    void getBudgetProgress_shouldCalculateExactlyOneHundredPercent() {
        ProgressFixture fixture = createProgressFixture();
        stubProgress(fixture, List.of(createTransaction(
                TransactionType.EXPENSE, "1000000", LocalDate.of(2026, 9, 10)
        )));

        BudgetProgressDto result = budgetService.getBudgetProgress(
                fixture.userId, fixture.budgetId
        );

        assertEquals(BigDecimal.ZERO, result.getRemainingAmount());
        assertEquals(new BigDecimal("100.00"), result.getPercentageUsed());
        assertFalse(result.getOverBudget());
    }

    @Test
    void getBudgetProgress_shouldCalculateOverBudget() {
        ProgressFixture fixture = createProgressFixture();
        stubProgress(fixture, List.of(
                createTransaction(TransactionType.EXPENSE, "600000", LocalDate.of(2026, 9, 10)),
                createTransaction(TransactionType.EXPENSE, "600000", LocalDate.of(2026, 9, 15))
        ));

        BudgetProgressDto result = budgetService.getBudgetProgress(
                fixture.userId, fixture.budgetId
        );

        assertEquals(new BigDecimal("1200000"), result.getSpentAmount());
        assertEquals(new BigDecimal("-200000"), result.getRemainingAmount());
        assertEquals(new BigDecimal("120.00"), result.getPercentageUsed());
        assertTrue(result.getOverBudget());
    }

    @Test
    void getBudgetProgress_shouldQueryOnlyTheBudgetMonth() {
        ProgressFixture fixture = arrangeProgress(List.of());

        budgetService.getBudgetProgress(fixture.userId, fixture.budgetId);

        verify(financialTransactionRepository)
                .findByCategoryIdAndUserIdAndTransactionDateGreaterThanEqualAndTransactionDateLessThan(
                        fixture.category.getId(),
                        fixture.userId,
                        LocalDate.of(2026, 9, 1),
                        LocalDate.of(2026, 10, 1)
                );
    }

    @Test
    void getBudgetProgress_shouldNotCountIncomeInSpentAmount() {
        ProgressFixture fixture = createProgressFixture();
        stubProgress(fixture, List.of(
                createTransaction(TransactionType.EXPENSE, "200000", LocalDate.of(2026, 9, 10)),
                createTransaction(TransactionType.INCOME, "500000", LocalDate.of(2026, 9, 12))
        ));

        BudgetProgressDto result = budgetService.getBudgetProgress(
                fixture.userId, fixture.budgetId
        );

        assertEquals(new BigDecimal("200000"), result.getSpentAmount());
        assertEquals(new BigDecimal("800000"), result.getRemainingAmount());
        assertEquals(new BigDecimal("20.00"), result.getPercentageUsed());
    }

    private void assertCreateRejectsInvalidValues(BudgetDto request) {
        Long userId = 1L;
        User user = createUser(userId);
        Category category = createCategory(20L, user, TransactionType.EXPENSE, true);
        stubUserAndCategory(userId, user, 20L, category);

        assertThrows(
                BusinessRuleException.class,
                () -> budgetService.createBudget(userId, request)
        );
        verify(budgetRepository, never()).save(any(Budget.class));
    }

    private void assertUpdateRejectsCategory(TransactionType type, Boolean active) {
        BudgetUpdateFixture fixture = createBudgetUpdateFixture();
        Category category = createCategory(20L, fixture.user, type, active);
        stubUpdateLookups(
                fixture.userId, fixture.budgetId, fixture.user,
                fixture.budget, category
        );

        assertThrows(
                BusinessRuleException.class,
                () -> budgetService.updateBudget(
                        fixture.userId, fixture.budgetId, fixture.request
                )
        );
        verify(budgetRepository, never()).save(any(Budget.class));
    }

    private void assertUpdateRejectsInvalidValues(BudgetDto request) {
        BudgetUpdateFixture fixture = createBudgetUpdateFixture();
        stubUpdateLookups(
                fixture.userId, fixture.budgetId, fixture.user,
                fixture.budget, fixture.category
        );

        assertThrows(
                BusinessRuleException.class,
                () -> budgetService.updateBudget(
                        fixture.userId, fixture.budgetId, request
                )
        );
        verify(budgetRepository, never()).save(any(Budget.class));
    }

    private void stubUserAndCategory(
            Long userId, User user, Long categoryId, Category category
    ) {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(categoryRepository.findByIdAndUserId(categoryId, userId))
                .thenReturn(Optional.of(category));
    }

    private void stubUpdateLookups(
            Long userId,
            Long budgetId,
            User user,
            Budget budget,
            Category category
    ) {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(budgetRepository.findByIdAndUserId(budgetId, userId))
                .thenReturn(Optional.of(budget));
        when(categoryRepository.findByIdAndUserId(category.getId(), userId))
                .thenReturn(Optional.of(category));
    }

    private ProgressFixture arrangeProgress(List<FinancialTransaction> transactions) {
        ProgressFixture fixture = createProgressFixture();
        stubProgress(fixture, transactions);
        return fixture;
    }

    private void stubProgress(
            ProgressFixture fixture,
            List<FinancialTransaction> transactions
    ) {
        when(userRepository.findById(fixture.userId))
                .thenReturn(Optional.of(fixture.budget.getUser()));
        when(budgetRepository.findByIdAndUserId(fixture.budgetId, fixture.userId))
                .thenReturn(Optional.of(fixture.budget));
        when(financialTransactionRepository
                .findByCategoryIdAndUserIdAndTransactionDateGreaterThanEqualAndTransactionDateLessThan(
                        fixture.category.getId(),
                        fixture.userId,
                        LocalDate.of(2026, 9, 1),
                        LocalDate.of(2026, 10, 1)
                )).thenReturn(transactions);
    }

    private BudgetUpdateFixture createBudgetUpdateFixture() {
        Long userId = 1L;
        Long budgetId = 100L;
        User user = createUser(userId);
        Category category = createCategory(20L, user, TransactionType.EXPENSE, true);
        Budget budget = createBudget(budgetId, user, category, 2026, 9, "1000000");
        BudgetDto request = createBudgetDto(2026, 9, "1000000", 20L);
        return new BudgetUpdateFixture(userId, budgetId, user, category, budget, request);
    }

    private ProgressFixture createProgressFixture() {
        Long userId = 1L;
        Long budgetId = 100L;
        User user = createUser(userId);
        Category category = createCategory(20L, user, TransactionType.EXPENSE, true);
        category.setName("Food");
        Budget budget = createBudget(budgetId, user, category, 2026, 9, "1000000");
        return new ProgressFixture(userId, budgetId, category, budget);
    }

    private User createUser(Long userId) {
        User user = new User();
        user.setId(userId);
        return user;
    }

    private Category createCategory(
            Long categoryId,
            User user,
            TransactionType type,
            Boolean active
    ) {
        Category category = new Category();
        category.setId(categoryId);
        category.setName("Food");
        category.setUser(user);
        category.setType(type);
        category.setActive(active);
        return category;
    }

    private Budget createBudget(
            Long budgetId,
            User user,
            Category category,
            Integer year,
            Integer month,
            String amount
    ) {
        Budget budget = new Budget();
        budget.setId(budgetId);
        budget.setUser(user);
        budget.setCategory(category);
        budget.setYear(year);
        budget.setMonth(month);
        budget.setAmount(new BigDecimal(amount));
        return budget;
    }

    private BudgetDto createBudgetDto(
            Integer year,
            Integer month,
            String amount,
            Long categoryId
    ) {
        BudgetDto budgetDto = new BudgetDto();
        budgetDto.setYear(year);
        budgetDto.setMonth(month);
        budgetDto.setAmount(amount == null ? null : new BigDecimal(amount));
        budgetDto.setCategoryId(categoryId);
        return budgetDto;
    }

    private FinancialTransaction createTransaction(
            TransactionType type,
            String amount,
            LocalDate transactionDate
    ) {
        FinancialTransaction transaction = new FinancialTransaction();
        transaction.setType(type);
        transaction.setAmount(new BigDecimal(amount));
        transaction.setTransactionDate(transactionDate);
        return transaction;
    }

    private record BudgetUpdateFixture(
            Long userId,
            Long budgetId,
            User user,
            Category category,
            Budget budget,
            BudgetDto request
    ) {
    }

    private record ProgressFixture(
            Long userId,
            Long budgetId,
            Category category,
            Budget budget
    ) {
    }
}
