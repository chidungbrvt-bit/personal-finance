package com.dung.personal_finance_backend.service;

import com.dung.personal_finance_backend.dto.FinancialTransactionDto;
import com.dung.personal_finance_backend.entity.Account;
import com.dung.personal_finance_backend.entity.Category;
import com.dung.personal_finance_backend.entity.FinancialTransaction;
import com.dung.personal_finance_backend.entity.User;
import com.dung.personal_finance_backend.enums.TransactionType;
import com.dung.personal_finance_backend.exception.AccountNotFoundException;
import com.dung.personal_finance_backend.exception.BusinessRuleException;
import com.dung.personal_finance_backend.exception.CategoryNotFoundException;
import com.dung.personal_finance_backend.exception.FinancialTransactionNotFoundException;
import com.dung.personal_finance_backend.exception.UserNotFoundException;
import com.dung.personal_finance_backend.repository.AccountRepository;
import com.dung.personal_finance_backend.repository.CategoryRepository;
import com.dung.personal_finance_backend.repository.FinancialTransactionRepository;
import com.dung.personal_finance_backend.repository.UserRepository;
import com.dung.personal_finance_backend.service.impl.FinancialTransactionServiceImpl;
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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinancialTransactionServiceImplTest {

    @Mock
    private FinancialTransactionRepository financialTransactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CategoryRepository categoryRepository;

    private FinancialTransactionServiceImpl financialTransactionService;

    @BeforeEach
    void setUp() {
        financialTransactionService =
                new FinancialTransactionServiceImpl(
                        financialTransactionRepository,
                        userRepository,
                        accountRepository,
                        categoryRepository
                );
    }

    @Test
    void createFinancialTransaction_shouldCreateExpenseTransaction() {
        Long userId = 1L;
        Long accountId = 10L;
        Long categoryId = 20L;

        User user = new User();
        user.setId(userId);

        Account account = new Account();
        account.setId(accountId);
        account.setUser(user);
        account.setActive(true);
        account.setInitialBalance(new BigDecimal("100.00"));

        Category category = new Category();
        category.setId(categoryId);
        category.setType(TransactionType.EXPENSE);
        category.setUser(user);
        category.setActive(true);

        FinancialTransactionDto request = new FinancialTransactionDto();
        request.setType(TransactionType.EXPENSE);
        request.setAmount(new BigDecimal("50.00"));
        request.setTransactionDate(LocalDate.of(2026, 9, 5));
        request.setDescription("Lunch");
        request.setAccountId(accountId);
        request.setCategoryId(categoryId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(accountRepository.findByIdAndUserId(accountId, userId))
                .thenReturn(Optional.of(account));

        when(categoryRepository.findByIdAndUserId(categoryId, userId))
                .thenReturn(Optional.of(category));

        when(financialTransactionRepository.save(
                any(FinancialTransaction.class)
        )).thenAnswer(invocation -> {
            FinancialTransaction transaction = invocation.getArgument(0);
            transaction.setId(100L);
            return transaction;
        });

        FinancialTransactionDto result =
                financialTransactionService.createFinancialTransaction(
                        userId,
                        request
                );

        assertEquals(100L, result.getId());
        assertEquals(TransactionType.EXPENSE, result.getType());
        assertEquals(new BigDecimal("50.00"), result.getAmount());
        assertEquals(accountId, result.getAccountId());
        assertEquals(categoryId, result.getCategoryId());

        verify(financialTransactionRepository)
                .save(any(FinancialTransaction.class));
    }

    @Test
    void createFinancialTransaction_shouldRejectCategoryTypeMismatch() {
        Long userId = 1L;
        Long accountId = 10L;
        Long categoryId = 20L;

        User user = new User();
        user.setId(userId);

        Account account = new Account();
        account.setId(accountId);
        account.setUser(user);
        account.setActive(true);

        Category incomeCategory = new Category();
        incomeCategory.setId(categoryId);
        incomeCategory.setType(TransactionType.INCOME);
        incomeCategory.setUser(user);
        incomeCategory.setActive(true);

        FinancialTransactionDto request = new FinancialTransactionDto();
        request.setType(TransactionType.EXPENSE);
        request.setAmount(new BigDecimal("50.00"));
        request.setTransactionDate(LocalDate.of(2026, 9, 5));
        request.setDescription("Lunch");
        request.setAccountId(accountId);
        request.setCategoryId(categoryId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(accountRepository.findByIdAndUserId(accountId, userId))
                .thenReturn(Optional.of(account));

        when(categoryRepository.findByIdAndUserId(categoryId, userId))
                .thenReturn(Optional.of(incomeCategory));

        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> financialTransactionService
                        .createFinancialTransaction(userId, request)
        );

        assertEquals(
                "Category type must match transaction type",
                exception.getMessage()
        );

        verify(financialTransactionRepository, never())
                .save(any(FinancialTransaction.class));
    }

    @Test
    void createFinancialTransaction_shouldRejectExpenseGreaterThanAccountBalance() {
        Long userId = 1L;
        Long accountId = 10L;
        User user = createUser(userId);
        Account account = createAccount(accountId, user, true);
        account.setInitialBalance(new BigDecimal("100.00"));
        Category category = createCategory(20L, user, TransactionType.EXPENSE, true);
        FinancialTransactionDto request = createTransactionDto(
                TransactionType.EXPENSE, new BigDecimal("100.01"), accountId, category.getId()
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(account));
        when(categoryRepository.findByIdAndUserId(category.getId(), userId)).thenReturn(Optional.of(category));
        when(financialTransactionRepository.findByAccountIdAndUserId(accountId, userId))
                .thenReturn(List.of());

        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> financialTransactionService.createFinancialTransaction(userId, request)
        );

        assertEquals("Insufficient account balance", exception.getMessage());
        verify(financialTransactionRepository, never()).save(any(FinancialTransaction.class));
    }

    @Test
    void createFinancialTransaction_shouldRejectAccountFromAnotherUser() {
        Long userId = 1L;
        Long accountId = 5L;

        User user = new User();
        user.setId(userId);

        FinancialTransactionDto transactionDto =
                new FinancialTransactionDto();

        transactionDto.setType(TransactionType.EXPENSE);
        transactionDto.setAmount(new BigDecimal("50.00"));
        transactionDto.setTransactionDate(
                LocalDate.of(2026, 9, 5)
        );
        transactionDto.setAccountId(accountId);
        transactionDto.setCategoryId(10L);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(accountRepository.findByIdAndUserId(accountId, userId))
                .thenReturn(Optional.empty());

        AccountNotFoundException exception = assertThrows(
                AccountNotFoundException.class,
                () -> financialTransactionService
                        .createFinancialTransaction(
                                userId,
                                transactionDto
                        )
        );

        assertEquals(
                "Account not found with id: " + accountId,
                exception.getMessage()
        );

        verify(financialTransactionRepository, never())
                .save(any(FinancialTransaction.class));
    }

    @Test
    void createFinancialTransaction_shouldRejectCategoryFromAnotherUser() {
        Long userId = 1L;
        Long accountId = 5L;
        Long categoryId = 10L;

        User user = new User();
        user.setId(userId);

        Account account = new Account();
        account.setId(accountId);
        account.setUser(user);
        account.setActive(true);

        FinancialTransactionDto transactionDto =
                new FinancialTransactionDto();

        transactionDto.setType(TransactionType.EXPENSE);
        transactionDto.setAmount(new BigDecimal("50.00"));
        transactionDto.setTransactionDate(
                LocalDate.of(2026, 9, 5)
        );
        transactionDto.setAccountId(accountId);
        transactionDto.setCategoryId(categoryId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(accountRepository.findByIdAndUserId(accountId, userId))
                .thenReturn(Optional.of(account));

        when(categoryRepository.findByIdAndUserId(categoryId, userId))
                .thenReturn(Optional.empty());

        CategoryNotFoundException exception = assertThrows(
                CategoryNotFoundException.class,
                () -> financialTransactionService
                        .createFinancialTransaction(
                                userId,
                                transactionDto
                        )
        );

        assertEquals(
                "Category not found with id: " + categoryId,
                exception.getMessage()
        );

        verify(financialTransactionRepository, never())
                .save(any(FinancialTransaction.class));
    }

    @Test
    void getFinancialTransactionsByAccountId_shouldReturnTransactionDtos() {
        Long userId = 1L;
        Long accountId = 5L;

        User user = new User();
        user.setId(userId);

        Account account = new Account();
        account.setId(accountId);
        account.setUser(user);

        Category foodCategory = new Category();
        foodCategory.setId(10L);
        foodCategory.setType(TransactionType.EXPENSE);
        foodCategory.setUser(user);

        FinancialTransaction firstTransaction =
                new FinancialTransaction();

        firstTransaction.setId(100L);
        firstTransaction.setType(TransactionType.EXPENSE);
        firstTransaction.setAmount(new BigDecimal("50.00"));
        firstTransaction.setTransactionDate(
                LocalDate.of(2026, 9, 5)
        );
        firstTransaction.setDescription("Lunch");
        firstTransaction.setAccount(account);
        firstTransaction.setCategory(foodCategory);
        firstTransaction.setUser(user);

        FinancialTransaction secondTransaction =
                new FinancialTransaction();

        secondTransaction.setId(101L);
        secondTransaction.setType(TransactionType.EXPENSE);
        secondTransaction.setAmount(new BigDecimal("20.00"));
        secondTransaction.setTransactionDate(
                LocalDate.of(2026, 9, 6)
        );
        secondTransaction.setDescription("Coffee");
        secondTransaction.setAccount(account);
        secondTransaction.setCategory(foodCategory);
        secondTransaction.setUser(user);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(accountRepository.findByIdAndUserId(accountId, userId))
                .thenReturn(Optional.of(account));

        when(financialTransactionRepository.findByAccountIdAndUserId(
                accountId,
                userId
        )).thenReturn(List.of(
                firstTransaction,
                secondTransaction
        ));

        List<FinancialTransactionDto> result =
                financialTransactionService
                        .getFinancialTransactionsByAccountId(
                                userId,
                                accountId
                        );

        assertEquals(2, result.size());

        assertEquals(100L, result.get(0).getId());
        assertEquals(
                new BigDecimal("50.00"),
                result.get(0).getAmount()
        );
        assertEquals(accountId, result.get(0).getAccountId());
        assertEquals(10L, result.get(0).getCategoryId());

        assertEquals(101L, result.get(1).getId());
        assertEquals(
                new BigDecimal("20.00"),
                result.get(1).getAmount()
        );

        verify(financialTransactionRepository)
                .findByAccountIdAndUserId(accountId, userId);
    }

    @Test
    void getFinancialTransactionsByCategoryId_shouldReturnTransactionDtos() {
        Long userId = 1L;
        Long accountId = 5L;
        Long categoryId = 10L;

        User user = new User();
        user.setId(userId);

        Account account = new Account();
        account.setId(accountId);
        account.setUser(user);

        Category category = new Category();
        category.setId(categoryId);
        category.setType(TransactionType.EXPENSE);
        category.setUser(user);

        FinancialTransaction transaction =
                new FinancialTransaction();

        transaction.setId(100L);
        transaction.setType(TransactionType.EXPENSE);
        transaction.setAmount(new BigDecimal("50.00"));
        transaction.setTransactionDate(
                LocalDate.of(2026, 9, 5)
        );
        transaction.setDescription("Lunch");
        transaction.setAccount(account);
        transaction.setCategory(category);
        transaction.setUser(user);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(categoryRepository.findByIdAndUserId(categoryId, userId))
                .thenReturn(Optional.of(category));

        when(financialTransactionRepository.findByCategoryIdAndUserId(
                categoryId,
                userId
        )).thenReturn(List.of(transaction));

        List<FinancialTransactionDto> result =
                financialTransactionService
                        .getFinancialTransactionsByCategoryId(
                                userId,
                                categoryId
                        );

        assertEquals(1, result.size());

        FinancialTransactionDto resultDto = result.get(0);

        assertEquals(100L, resultDto.getId());
        assertEquals(TransactionType.EXPENSE, resultDto.getType());
        assertEquals(
                new BigDecimal("50.00"),
                resultDto.getAmount()
        );
        assertEquals(accountId, resultDto.getAccountId());
        assertEquals(categoryId, resultDto.getCategoryId());
        assertEquals("Lunch", resultDto.getDescription());

        verify(financialTransactionRepository)
                .findByCategoryIdAndUserId(categoryId, userId);
    }

    @Test
    void createFinancialTransaction_shouldThrowWhenUserDoesNotExist() {
        Long userId = 999L;
        FinancialTransactionDto request = createTransactionDto(
                TransactionType.EXPENSE,
                new BigDecimal("50.00"),
                10L,
                20L
        );

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> financialTransactionService.createFinancialTransaction(userId, request)
        );

        verify(accountRepository, never()).findByIdAndUserId(10L, userId);
        verify(financialTransactionRepository, never())
                .save(any(FinancialTransaction.class));
    }

    @Test
    void createFinancialTransaction_shouldRejectInactiveAccount() {
        Long userId = 1L;
        Long accountId = 10L;
        User user = createUser(userId);
        Account inactiveAccount = createAccount(accountId, user, false);
        FinancialTransactionDto request = createTransactionDto(
                TransactionType.EXPENSE,
                new BigDecimal("50.00"),
                accountId,
                20L
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(accountRepository.findByIdAndUserId(accountId, userId))
                .thenReturn(Optional.of(inactiveAccount));

        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> financialTransactionService.createFinancialTransaction(userId, request)
        );

        assertEquals("Cannot use an inactive account", exception.getMessage());
        verify(categoryRepository, never()).findByIdAndUserId(20L, userId);
        verify(financialTransactionRepository, never())
                .save(any(FinancialTransaction.class));
    }

    @Test
    void createFinancialTransaction_shouldRejectInactiveCategory() {
        Long userId = 1L;
        Long accountId = 10L;
        Long categoryId = 20L;
        User user = createUser(userId);
        Account account = createAccount(accountId, user, true);
        Category inactiveCategory = createCategory(
                categoryId,
                user,
                TransactionType.EXPENSE,
                false
        );
        FinancialTransactionDto request = createTransactionDto(
                TransactionType.EXPENSE,
                new BigDecimal("50.00"),
                accountId,
                categoryId
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(accountRepository.findByIdAndUserId(accountId, userId))
                .thenReturn(Optional.of(account));
        when(categoryRepository.findByIdAndUserId(categoryId, userId))
                .thenReturn(Optional.of(inactiveCategory));

        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> financialTransactionService.createFinancialTransaction(userId, request)
        );

        assertEquals("Cannot use an inactive category", exception.getMessage());
        verify(financialTransactionRepository, never())
                .save(any(FinancialTransaction.class));
    }

    @Test
    void createFinancialTransaction_shouldRejectZeroAmount() {
        Long userId = 1L;
        FinancialTransactionDto request = arrangeValidCreateRequest(
                userId,
                TransactionType.EXPENSE,
                BigDecimal.ZERO
        );

        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> financialTransactionService.createFinancialTransaction(userId, request)
        );

        assertEquals("Transaction amount must be greater than 0", exception.getMessage());
        verify(financialTransactionRepository, never())
                .save(any(FinancialTransaction.class));
    }

    @Test
    void createFinancialTransaction_shouldRejectNegativeAmount() {
        Long userId = 1L;
        FinancialTransactionDto request = arrangeValidCreateRequest(
                userId,
                TransactionType.EXPENSE,
                new BigDecimal("-1.00")
        );

        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> financialTransactionService.createFinancialTransaction(userId, request)
        );

        assertEquals("Transaction amount must be greater than 0", exception.getMessage());
        verify(financialTransactionRepository, never())
                .save(any(FinancialTransaction.class));
    }

    @Test
    void createFinancialTransaction_shouldRejectNullAmount() {
        Long userId = 1L;
        FinancialTransactionDto request = arrangeValidCreateRequest(
                userId,
                TransactionType.EXPENSE,
                null
        );

        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> financialTransactionService.createFinancialTransaction(userId, request)
        );

        assertEquals("Transaction amount must be greater than 0", exception.getMessage());
        verify(financialTransactionRepository, never())
                .save(any(FinancialTransaction.class));
    }

    @Test
    void createFinancialTransaction_shouldCreateIncomeTransaction() {
        Long userId = 1L;
        FinancialTransactionDto request = arrangeValidCreateRequest(
                userId,
                TransactionType.INCOME,
                new BigDecimal("500.00")
        );

        when(financialTransactionRepository.save(any(FinancialTransaction.class)))
                .thenAnswer(invocation -> {
                    FinancialTransaction transaction = invocation.getArgument(0);
                    transaction.setId(101L);
                    return transaction;
                });

        FinancialTransactionDto result =
                financialTransactionService.createFinancialTransaction(userId, request);

        assertEquals(101L, result.getId());
        assertEquals(TransactionType.INCOME, result.getType());
        assertEquals(new BigDecimal("500.00"), result.getAmount());
        assertEquals(10L, result.getAccountId());
        assertEquals(20L, result.getCategoryId());
        verify(financialTransactionRepository)
                .save(any(FinancialTransaction.class));
    }

    @Test
    void getFinancialTransactionsByUserId_shouldReturnAllUserTransactions() {
        Long userId = 1L;
        User user = createUser(userId);
        Account account = createAccount(10L, user, true);
        Category category = createCategory(20L, user, TransactionType.EXPENSE, true);
        FinancialTransaction transaction = createTransaction(
                100L, user, account, category, TransactionType.EXPENSE,
                new BigDecimal("25.00")
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(financialTransactionRepository.findByUserId(userId))
                .thenReturn(List.of(transaction));

        List<FinancialTransactionDto> result =
                financialTransactionService.getFinancialTransactionsByUserId(userId);

        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getId());
        assertEquals(10L, result.get(0).getAccountId());
        assertEquals(20L, result.get(0).getCategoryId());
    }

    @Test
    void getFinancialTransactionById_shouldReturnTransactionSuccessfully() {
        Long userId = 1L;
        Long transactionId = 100L;
        User user = createUser(userId);
        Account account = createAccount(10L, user, true);
        Category category = createCategory(20L, user, TransactionType.EXPENSE, true);
        FinancialTransaction transaction = createTransaction(
                transactionId, user, account, category, TransactionType.EXPENSE,
                new BigDecimal("25.00")
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(financialTransactionRepository.findByIdAndUserId(transactionId, userId))
                .thenReturn(Optional.of(transaction));

        FinancialTransactionDto result =
                financialTransactionService.getFinancialTransactionById(userId, transactionId);

        assertEquals(transactionId, result.getId());
        assertEquals(new BigDecimal("25.00"), result.getAmount());
        assertEquals(10L, result.getAccountId());
        assertEquals(20L, result.getCategoryId());
    }

    @Test
    void getFinancialTransactionById_shouldRejectMissingOrAnotherUsersTransaction() {
        Long userId = 1L;
        Long transactionId = 999L;

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(createUser(userId)));
        when(financialTransactionRepository.findByIdAndUserId(transactionId, userId))
                .thenReturn(Optional.empty());

        assertThrows(
                FinancialTransactionNotFoundException.class,
                () -> financialTransactionService
                        .getFinancialTransactionById(userId, transactionId)
        );
    }

    @Test
    void getFinancialTransactionsByAccountId_shouldRejectAccountFromAnotherUser() {
        Long userId = 1L;
        Long accountId = 10L;

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(createUser(userId)));
        when(accountRepository.findByIdAndUserId(accountId, userId))
                .thenReturn(Optional.empty());

        assertThrows(
                AccountNotFoundException.class,
                () -> financialTransactionService
                        .getFinancialTransactionsByAccountId(userId, accountId)
        );

        verify(financialTransactionRepository, never())
                .findByAccountIdAndUserId(accountId, userId);
    }

    @Test
    void getFinancialTransactionsByCategoryId_shouldRejectCategoryFromAnotherUser() {
        Long userId = 1L;
        Long categoryId = 20L;

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(createUser(userId)));
        when(categoryRepository.findByIdAndUserId(categoryId, userId))
                .thenReturn(Optional.empty());

        assertThrows(
                CategoryNotFoundException.class,
                () -> financialTransactionService
                        .getFinancialTransactionsByCategoryId(userId, categoryId)
        );

        verify(financialTransactionRepository, never())
                .findByCategoryIdAndUserId(categoryId, userId);
    }

    @Test
    void getFinancialTransactionsByType_shouldReturnMatchingTransactions() {
        Long userId = 1L;
        User user = createUser(userId);
        Account account = createAccount(10L, user, true);
        Category category = createCategory(20L, user, TransactionType.INCOME, true);
        FinancialTransaction income = createTransaction(
                100L, user, account, category, TransactionType.INCOME,
                new BigDecimal("500.00")
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(financialTransactionRepository.findByUserIdAndType(
                userId, TransactionType.INCOME
        )).thenReturn(List.of(income));

        List<FinancialTransactionDto> result =
                financialTransactionService.getFinancialTransactionsByType(
                        userId, TransactionType.INCOME
                );

        assertEquals(1, result.size());
        assertEquals(TransactionType.INCOME, result.get(0).getType());
        verify(financialTransactionRepository)
                .findByUserIdAndType(userId, TransactionType.INCOME);
    }

    @Test
    void updateFinancialTransaction_shouldMutateExistingTransactionAndSaveIt() {
        Long userId = 1L;
        Long transactionId = 100L;
        User user = createUser(userId);
        Account oldAccount = createAccount(1L, user, true);
        Category oldCategory = createCategory(2L, user, TransactionType.EXPENSE, true);
        FinancialTransaction existing = createTransaction(
                transactionId, user, oldAccount, oldCategory,
                TransactionType.EXPENSE, new BigDecimal("10.00")
        );
        Account newAccount = createAccount(10L, user, true);
        Category newCategory = createCategory(20L, user, TransactionType.EXPENSE, true);
        FinancialTransactionDto request = createTransactionDto(
                TransactionType.EXPENSE,
                new BigDecimal("75.00"),
                10L,
                20L
        );
        request.setDescription("Updated transaction");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(financialTransactionRepository.findByIdAndUserId(transactionId, userId))
                .thenReturn(Optional.of(existing));
        when(accountRepository.findByIdAndUserId(10L, userId))
                .thenReturn(Optional.of(newAccount));
        when(categoryRepository.findByIdAndUserId(20L, userId))
                .thenReturn(Optional.of(newCategory));
        when(financialTransactionRepository.save(existing)).thenReturn(existing);

        FinancialTransactionDto result = financialTransactionService
                .updateFinancialTransaction(userId, transactionId, request);

        assertEquals(new BigDecimal("75.00"), existing.getAmount());
        assertEquals("Updated transaction", existing.getDescription());
        assertSame(newAccount, existing.getAccount());
        assertSame(newCategory, existing.getCategory());
        assertEquals(transactionId, result.getId());
        verify(financialTransactionRepository).save(existing);
    }

    @Test
    void updateFinancialTransaction_shouldRejectMissingTransaction() {
        Long userId = 1L;
        Long transactionId = 999L;
        FinancialTransactionDto request = createTransactionDto(
                TransactionType.EXPENSE, new BigDecimal("50.00"), 10L, 20L
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(createUser(userId)));
        when(financialTransactionRepository.findByIdAndUserId(transactionId, userId))
                .thenReturn(Optional.empty());

        assertThrows(
                FinancialTransactionNotFoundException.class,
                () -> financialTransactionService
                        .updateFinancialTransaction(userId, transactionId, request)
        );
        verify(financialTransactionRepository, never())
                .save(any(FinancialTransaction.class));
    }

    @Test
    void updateFinancialTransaction_shouldRejectNewAccountFromAnotherUser() {
        Long userId = 1L;
        Long transactionId = 100L;
        User user = createUser(userId);
        FinancialTransaction existing = new FinancialTransaction();
        existing.setId(transactionId);
        FinancialTransactionDto request = createTransactionDto(
                TransactionType.EXPENSE, new BigDecimal("50.00"), 10L, 20L
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(financialTransactionRepository.findByIdAndUserId(transactionId, userId))
                .thenReturn(Optional.of(existing));
        when(accountRepository.findByIdAndUserId(10L, userId))
                .thenReturn(Optional.empty());

        assertThrows(
                AccountNotFoundException.class,
                () -> financialTransactionService
                        .updateFinancialTransaction(userId, transactionId, request)
        );
        verify(financialTransactionRepository, never()).save(existing);
    }

    @Test
    void updateFinancialTransaction_shouldRejectInactiveNewAccount() {
        Long userId = 1L;
        Long transactionId = 100L;
        User user = createUser(userId);
        FinancialTransaction existing = new FinancialTransaction();
        Account inactiveAccount = createAccount(10L, user, false);
        FinancialTransactionDto request = createTransactionDto(
                TransactionType.EXPENSE, new BigDecimal("50.00"), 10L, 20L
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(financialTransactionRepository.findByIdAndUserId(transactionId, userId))
                .thenReturn(Optional.of(existing));
        when(accountRepository.findByIdAndUserId(10L, userId))
                .thenReturn(Optional.of(inactiveAccount));

        assertThrows(
                BusinessRuleException.class,
                () -> financialTransactionService
                        .updateFinancialTransaction(userId, transactionId, request)
        );
        verify(financialTransactionRepository, never()).save(existing);
    }

    @Test
    void updateFinancialTransaction_shouldRejectNewCategoryFromAnotherUser() {
        Long userId = 1L;
        Long transactionId = 100L;
        User user = createUser(userId);
        Account account = createAccount(10L, user, true);
        FinancialTransaction existing = new FinancialTransaction();
        FinancialTransactionDto request = createTransactionDto(
                TransactionType.EXPENSE, new BigDecimal("50.00"), 10L, 20L
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(financialTransactionRepository.findByIdAndUserId(transactionId, userId))
                .thenReturn(Optional.of(existing));
        when(accountRepository.findByIdAndUserId(10L, userId))
                .thenReturn(Optional.of(account));
        when(categoryRepository.findByIdAndUserId(20L, userId))
                .thenReturn(Optional.empty());

        assertThrows(
                CategoryNotFoundException.class,
                () -> financialTransactionService
                        .updateFinancialTransaction(userId, transactionId, request)
        );
        verify(financialTransactionRepository, never()).save(existing);
    }

    @Test
    void updateFinancialTransaction_shouldRejectInactiveNewCategory() {
        Long userId = 1L;
        Long transactionId = 100L;
        User user = createUser(userId);
        Account account = createAccount(10L, user, true);
        Category category = createCategory(20L, user, TransactionType.EXPENSE, false);
        FinancialTransaction existing = new FinancialTransaction();
        FinancialTransactionDto request = createTransactionDto(
                TransactionType.EXPENSE, new BigDecimal("50.00"), 10L, 20L
        );

        stubUpdateLookup(userId, transactionId, user, existing, account, category);

        assertThrows(
                BusinessRuleException.class,
                () -> financialTransactionService
                        .updateFinancialTransaction(userId, transactionId, request)
        );
        verify(financialTransactionRepository, never()).save(existing);
    }

    @Test
    void updateFinancialTransaction_shouldRejectCategoryTypeMismatch() {
        Long userId = 1L;
        Long transactionId = 100L;
        User user = createUser(userId);
        Account account = createAccount(10L, user, true);
        Category category = createCategory(20L, user, TransactionType.INCOME, true);
        FinancialTransaction existing = new FinancialTransaction();
        FinancialTransactionDto request = createTransactionDto(
                TransactionType.EXPENSE, new BigDecimal("50.00"), 10L, 20L
        );

        stubUpdateLookup(userId, transactionId, user, existing, account, category);

        assertThrows(
                BusinessRuleException.class,
                () -> financialTransactionService
                        .updateFinancialTransaction(userId, transactionId, request)
        );
        verify(financialTransactionRepository, never()).save(existing);
    }

    @Test
    void updateFinancialTransaction_shouldRejectZeroAmount() {
        assertUpdateRejectsInvalidAmount(BigDecimal.ZERO);
    }

    @Test
    void updateFinancialTransaction_shouldRejectNegativeAmount() {
        assertUpdateRejectsInvalidAmount(new BigDecimal("-1.00"));
    }

    @Test
    void updateFinancialTransaction_shouldRejectNullAmount() {
        assertUpdateRejectsInvalidAmount(null);
    }

    @Test
    void deleteFinancialTransaction_shouldDeleteExistingTransaction() {
        Long userId = 1L;
        Long transactionId = 100L;
        User user = createUser(userId);
        FinancialTransaction transaction = new FinancialTransaction();
        transaction.setId(transactionId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(financialTransactionRepository.findByIdAndUserId(transactionId, userId))
                .thenReturn(Optional.of(transaction));

        financialTransactionService.deleteFinancialTransaction(userId, transactionId);

        verify(financialTransactionRepository).delete(transaction);
    }

    @Test
    void deleteFinancialTransaction_shouldRejectMissingTransaction() {
        Long userId = 1L;
        Long transactionId = 999L;

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(createUser(userId)));
        when(financialTransactionRepository.findByIdAndUserId(transactionId, userId))
                .thenReturn(Optional.empty());

        assertThrows(
                FinancialTransactionNotFoundException.class,
                () -> financialTransactionService
                        .deleteFinancialTransaction(userId, transactionId)
        );
        verify(financialTransactionRepository, never())
                .delete(any(FinancialTransaction.class));
    }

    @Test
    void getFinancialTransactionsByDateRange_shouldCallRepositoryAndMapResult() {
        Long userId = 1L;
        LocalDate startDate = LocalDate.of(2026, 9, 1);
        LocalDate endDate = LocalDate.of(2026, 10, 1);
        User user = createUser(userId);
        FinancialTransaction transaction = createMappedTransaction(user);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(financialTransactionRepository
                .findByUserIdAndTransactionDateGreaterThanEqualAndTransactionDateLessThan(
                        userId, startDate, endDate
                )).thenReturn(List.of(transaction));

        List<FinancialTransactionDto> result = financialTransactionService
                .getFinancialTransactionsByDateRange(userId, startDate, endDate);

        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getId());
        verify(financialTransactionRepository)
                .findByUserIdAndTransactionDateGreaterThanEqualAndTransactionDateLessThan(
                        userId, startDate, endDate
                );
    }

    @Test
    void getFinancialTransactionsByAccountAndDateRange_shouldCallRepositoryAndMapResult() {
        Long userId = 1L;
        Long accountId = 10L;
        LocalDate startDate = LocalDate.of(2026, 9, 1);
        LocalDate endDate = LocalDate.of(2026, 10, 1);
        User user = createUser(userId);
        Account account = createAccount(accountId, user, true);
        FinancialTransaction transaction = createMappedTransaction(user);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(accountRepository.findByIdAndUserId(accountId, userId))
                .thenReturn(Optional.of(account));
        when(financialTransactionRepository
                .findByAccountIdAndUserIdAndTransactionDateGreaterThanEqualAndTransactionDateLessThan(
                        accountId, userId, startDate, endDate
                )).thenReturn(List.of(transaction));

        List<FinancialTransactionDto> result = financialTransactionService
                .getFinancialTransactionsByAccountAndDateRange(
                        userId, accountId, startDate, endDate
                );

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getAccountId());
        verify(financialTransactionRepository)
                .findByAccountIdAndUserIdAndTransactionDateGreaterThanEqualAndTransactionDateLessThan(
                        accountId, userId, startDate, endDate
                );
    }

    @Test
    void getFinancialTransactionsByCategoryAndDateRange_shouldCallRepositoryAndMapResult() {
        Long userId = 1L;
        Long categoryId = 20L;
        LocalDate startDate = LocalDate.of(2026, 9, 1);
        LocalDate endDate = LocalDate.of(2026, 10, 1);
        User user = createUser(userId);
        Category category = createCategory(
                categoryId, user, TransactionType.EXPENSE, true
        );
        FinancialTransaction transaction = createMappedTransaction(user);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(categoryRepository.findByIdAndUserId(categoryId, userId))
                .thenReturn(Optional.of(category));
        when(financialTransactionRepository
                .findByCategoryIdAndUserIdAndTransactionDateGreaterThanEqualAndTransactionDateLessThan(
                        categoryId, userId, startDate, endDate
                )).thenReturn(List.of(transaction));

        List<FinancialTransactionDto> result = financialTransactionService
                .getFinancialTransactionsByCategoryAndDateRange(
                        userId, categoryId, startDate, endDate
                );

        assertEquals(1, result.size());
        assertEquals(20L, result.get(0).getCategoryId());
        verify(financialTransactionRepository)
                .findByCategoryIdAndUserIdAndTransactionDateGreaterThanEqualAndTransactionDateLessThan(
                        categoryId, userId, startDate, endDate
                );
    }

    @Test
    void getFinancialTransactionsByTypeAndDateRange_shouldCallRepositoryAndMapResult() {
        Long userId = 1L;
        LocalDate startDate = LocalDate.of(2026, 9, 1);
        LocalDate endDate = LocalDate.of(2026, 10, 1);
        User user = createUser(userId);
        FinancialTransaction transaction = createMappedTransaction(user);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(financialTransactionRepository
                .findByUserIdAndTypeAndTransactionDateGreaterThanEqualAndTransactionDateLessThan(
                        userId, TransactionType.EXPENSE, startDate, endDate
                )).thenReturn(List.of(transaction));

        List<FinancialTransactionDto> result = financialTransactionService
                .getFinancialTransactionsByTypeAndDateRange(
                        userId, TransactionType.EXPENSE, startDate, endDate
                );

        assertEquals(1, result.size());
        assertEquals(TransactionType.EXPENSE, result.get(0).getType());
        verify(financialTransactionRepository)
                .findByUserIdAndTypeAndTransactionDateGreaterThanEqualAndTransactionDateLessThan(
                        userId, TransactionType.EXPENSE, startDate, endDate
                );
    }

    private void assertUpdateRejectsInvalidAmount(BigDecimal amount) {
        Long userId = 1L;
        Long transactionId = 100L;
        User user = createUser(userId);
        Account account = createAccount(10L, user, true);
        Category category = createCategory(20L, user, TransactionType.EXPENSE, true);
        FinancialTransaction existing = new FinancialTransaction();
        FinancialTransactionDto request = createTransactionDto(
                TransactionType.EXPENSE, amount, 10L, 20L
        );

        stubUpdateLookup(userId, transactionId, user, existing, account, category);

        assertThrows(
                BusinessRuleException.class,
                () -> financialTransactionService
                        .updateFinancialTransaction(userId, transactionId, request)
        );
        verify(financialTransactionRepository, never()).save(existing);
    }

    private void stubUpdateLookup(
            Long userId,
            Long transactionId,
            User user,
            FinancialTransaction existing,
            Account account,
            Category category
    ) {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(financialTransactionRepository.findByIdAndUserId(transactionId, userId))
                .thenReturn(Optional.of(existing));
        when(accountRepository.findByIdAndUserId(account.getId(), userId))
                .thenReturn(Optional.of(account));
        when(categoryRepository.findByIdAndUserId(category.getId(), userId))
                .thenReturn(Optional.of(category));
    }

    private FinancialTransaction createMappedTransaction(User user) {
        Account account = createAccount(10L, user, true);
        Category category = createCategory(20L, user, TransactionType.EXPENSE, true);
        return createTransaction(
                100L, user, account, category,
                TransactionType.EXPENSE, new BigDecimal("25.00")
        );
    }

    private FinancialTransaction createTransaction(
            Long transactionId,
            User user,
            Account account,
            Category category,
            TransactionType type,
            BigDecimal amount
    ) {
        FinancialTransaction transaction = new FinancialTransaction();
        transaction.setId(transactionId);
        transaction.setUser(user);
        transaction.setAccount(account);
        transaction.setCategory(category);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setTransactionDate(LocalDate.of(2026, 9, 6));
        transaction.setDescription("Test transaction");
        return transaction;
    }

    private FinancialTransactionDto arrangeValidCreateRequest(
            Long userId,
            TransactionType type,
            BigDecimal amount
    ) {
        Long accountId = 10L;
        Long categoryId = 20L;
        User user = createUser(userId);
        Account account = createAccount(accountId, user, true);
        Category category = createCategory(categoryId, user, type, true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(accountRepository.findByIdAndUserId(accountId, userId))
                .thenReturn(Optional.of(account));
        when(categoryRepository.findByIdAndUserId(categoryId, userId))
                .thenReturn(Optional.of(category));

        return createTransactionDto(type, amount, accountId, categoryId);
    }

    private FinancialTransactionDto createTransactionDto(
            TransactionType type,
            BigDecimal amount,
            Long accountId,
            Long categoryId
    ) {
        FinancialTransactionDto transactionDto = new FinancialTransactionDto();
        transactionDto.setType(type);
        transactionDto.setAmount(amount);
        transactionDto.setTransactionDate(LocalDate.of(2026, 9, 6));
        transactionDto.setDescription("Test transaction");
        transactionDto.setAccountId(accountId);
        transactionDto.setCategoryId(categoryId);
        return transactionDto;
    }

    private User createUser(Long userId) {
        User user = new User();
        user.setId(userId);
        return user;
    }

    private Account createAccount(Long accountId, User user, Boolean active) {
        Account account = new Account();
        account.setId(accountId);
        account.setUser(user);
        account.setActive(active);
        account.setInitialBalance(new BigDecimal("1000.00"));
        return account;
    }

    private Category createCategory(
            Long categoryId,
            User user,
            TransactionType type,
            Boolean active
    ) {
        Category category = new Category();
        category.setId(categoryId);
        category.setUser(user);
        category.setType(type);
        category.setActive(active);
        return category;
    }
}
