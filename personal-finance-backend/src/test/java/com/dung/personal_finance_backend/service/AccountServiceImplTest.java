package com.dung.personal_finance_backend.service;

import com.dung.personal_finance_backend.dto.AccountDto;
import com.dung.personal_finance_backend.entity.Account;
import com.dung.personal_finance_backend.entity.FinancialTransaction;
import com.dung.personal_finance_backend.entity.User;
import com.dung.personal_finance_backend.enums.AccountType;
import com.dung.personal_finance_backend.enums.CurrencyType;
import com.dung.personal_finance_backend.enums.TransactionType;
import com.dung.personal_finance_backend.exception.AccountNotFoundException;
import com.dung.personal_finance_backend.exception.BusinessRuleException;
import com.dung.personal_finance_backend.exception.UserNotFoundException;
import com.dung.personal_finance_backend.repository.AccountRepository;
import com.dung.personal_finance_backend.repository.FinancialTransactionRepository;
import com.dung.personal_finance_backend.repository.UserRepository;
import com.dung.personal_finance_backend.service.impl.AccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FinancialTransactionRepository financialTransactionRepository;

    private AccountServiceImpl accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountServiceImpl(
                accountRepository,
                userRepository,
                financialTransactionRepository
        );
    }

    @Test
    void createAccount_shouldCreateAccountSuccessfully() {
        Long userId = 1L;
        User user = createUser(userId);
        AccountDto accountDto = createAccountDto(new BigDecimal("1000.00"), true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account account = invocation.getArgument(0);
            account.setId(10L);
            return account;
        });

        AccountDto result = accountService.createAccount(userId, accountDto);

        assertEquals(10L, result.getId());
        assertEquals("Cash wallet", result.getName());
        assertEquals(AccountType.CASH, result.getType());
        assertEquals(new BigDecimal("1000.00"), result.getInitialBalance());
        assertEquals(CurrencyType.VND, result.getCurrency());
        assertTrue(result.getActive());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void createAccount_shouldRejectNullInitialBalance() {
        Long userId = 1L;
        AccountDto accountDto = createAccountDto(null, true);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(createUser(userId)));

        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> accountService.createAccount(userId, accountDto)
        );

        assertEquals("Initial balance is required", exception.getMessage());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void createAccount_shouldThrowWhenUserDoesNotExist() {
        Long userId = 999L;
        AccountDto accountDto = createAccountDto(BigDecimal.ZERO, true);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> accountService.createAccount(userId, accountDto)
        );

        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void createAccount_shouldDefaultActiveToTrue() {
        Long userId = 1L;
        AccountDto accountDto = createAccountDto(BigDecimal.ZERO, null);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(createUser(userId)));
        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AccountDto result = accountService.createAccount(userId, accountDto);

        assertTrue(result.getActive());
    }

    @Test
    void createAccount_shouldRejectNonVndCurrency() {
        Long userId = 1L;
        AccountDto accountDto = new AccountDto(
                null,
                "Dollar wallet",
                AccountType.CASH,
                BigDecimal.ZERO,
                CurrencyType.USD,
                true,
                null
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(createUser(userId)));

        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> accountService.createAccount(userId, accountDto)
        );

        assertEquals("Only VND currency is supported", exception.getMessage());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void getAccountsByUserId_shouldReturnOnlyActiveAccounts() {
        Long userId = 1L;
        User user = createUser(userId);
        Account activeAccount = createAccount(10L, user, new BigDecimal("100.00"), true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(accountRepository.findByUserIdAndActiveTrue(userId))
                .thenReturn(List.of(activeAccount));

        List<AccountDto> result = accountService.getAccountsByUserId(userId);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getId());
        assertTrue(result.get(0).getActive());
        verify(accountRepository).findByUserIdAndActiveTrue(userId);
    }

    @Test
    void getAccountById_shouldReturnAccountSuccessfully() {
        Long userId = 1L;
        Long accountId = 10L;
        User user = createUser(userId);
        Account account = createAccount(accountId, user, new BigDecimal("100.00"), true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(accountRepository.findByIdAndUserId(accountId, userId))
                .thenReturn(Optional.of(account));

        AccountDto result = accountService.getAccountById(userId, accountId);

        assertEquals(accountId, result.getId());
        assertEquals("Cash wallet", result.getName());
    }

    @Test
    void getAccountById_shouldRejectAccountFromAnotherUser() {
        Long userId = 1L;
        Long accountId = 10L;

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(createUser(userId)));
        when(accountRepository.findByIdAndUserId(accountId, userId))
                .thenReturn(Optional.empty());

        assertThrows(
                AccountNotFoundException.class,
                () -> accountService.getAccountById(userId, accountId)
        );
    }

    @Test
    void updateAccount_shouldUpdateAccountSuccessfully() {
        Long userId = 1L;
        Long accountId = 10L;
        User user = createUser(userId);
        Account account = createAccount(accountId, user, BigDecimal.ZERO, true);
        AccountDto updateDto = new AccountDto(
                null,
                "Main bank",
                AccountType.BANK,
                new BigDecimal("5000.00"),
                CurrencyType.VND,
                true,
                null
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(accountRepository.findByIdAndUserId(accountId, userId))
                .thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);

        AccountDto result = accountService.updateAccount(userId, accountId, updateDto);

        assertEquals("Main bank", result.getName());
        assertEquals(AccountType.BANK, result.getType());
        assertEquals(new BigDecimal("5000.00"), result.getInitialBalance());
        assertEquals(CurrencyType.VND, result.getCurrency());
        verify(accountRepository).save(account);
    }

    @Test
    void updateAccount_shouldRejectNonVndCurrency() {
        Long userId = 1L;
        Long accountId = 10L;
        User user = createUser(userId);
        Account account = createAccount(accountId, user, BigDecimal.ZERO, true);
        AccountDto updateDto = new AccountDto(
                null,
                "Dollar wallet",
                AccountType.BANK,
                new BigDecimal("100.00"),
                CurrencyType.USD,
                true,
                null
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(accountRepository.findByIdAndUserId(accountId, userId))
                .thenReturn(Optional.of(account));

        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> accountService.updateAccount(userId, accountId, updateDto)
        );

        assertEquals("Only VND currency is supported", exception.getMessage());
        assertEquals(CurrencyType.VND, account.getCurrency());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void deleteAccount_shouldHardDeleteWhenAccountHasNoTransactions() {
        Long userId = 1L;
        Long accountId = 10L;
        User user = createUser(userId);
        Account account = createAccount(accountId, user, BigDecimal.ZERO, true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(accountRepository.findByIdAndUserId(accountId, userId))
                .thenReturn(Optional.of(account));
        when(financialTransactionRepository.existsByAccountIdAndUserId(accountId, userId))
                .thenReturn(false);

        accountService.deleteAccount(userId, accountId);

        verify(accountRepository).delete(account);
        verify(accountRepository, never()).save(account);
    }

    @Test
    void deleteAccount_shouldArchiveWhenAccountHasTransactions() {
        Long userId = 1L;
        Long accountId = 10L;
        User user = createUser(userId);
        Account account = createAccount(accountId, user, BigDecimal.ZERO, true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(accountRepository.findByIdAndUserId(accountId, userId))
                .thenReturn(Optional.of(account));
        when(financialTransactionRepository.existsByAccountIdAndUserId(accountId, userId))
                .thenReturn(true);

        accountService.deleteAccount(userId, accountId);

        assertFalse(account.getActive());
        verify(accountRepository).save(account);
        verify(accountRepository, never()).delete(account);
    }

    @Test
    void getAccountBalance_shouldAddIncomeAndSubtractExpenses() {
        Long userId = 1L;
        Long accountId = 10L;
        User user = createUser(userId);
        Account account = createAccount(
                accountId,
                user,
                new BigDecimal("1000.00"),
                true
        );

        FinancialTransaction income = new FinancialTransaction();
        income.setType(TransactionType.INCOME);
        income.setAmount(new BigDecimal("500.00"));

        FinancialTransaction expense = new FinancialTransaction();
        expense.setType(TransactionType.EXPENSE);
        expense.setAmount(new BigDecimal("200.00"));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(accountRepository.findByIdAndUserId(accountId, userId))
                .thenReturn(Optional.of(account));
        when(financialTransactionRepository.findByAccountIdAndUserId(accountId, userId))
                .thenReturn(List.of(income, expense));

        BigDecimal result = accountService.getAccountBalance(userId, accountId);

        assertEquals(new BigDecimal("1300.00"), result);
    }

    @Test
    void getAccountBalance_shouldReturnInitialBalanceWhenThereAreNoTransactions() {
        Long userId = 1L;
        Long accountId = 10L;
        User user = createUser(userId);
        Account account = createAccount(
                accountId,
                user,
                new BigDecimal("1000.00"),
                true
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(accountRepository.findByIdAndUserId(accountId, userId))
                .thenReturn(Optional.of(account));
        when(financialTransactionRepository.findByAccountIdAndUserId(accountId, userId))
                .thenReturn(List.of());

        BigDecimal result = accountService.getAccountBalance(userId, accountId);

        assertEquals(new BigDecimal("1000.00"), result);
    }

    @Test
    void getAccountBalance_shouldStartFromZeroWhenInitialBalanceIsNull() {
        Long userId = 1L;
        Long accountId = 10L;
        User user = createUser(userId);
        Account account = createAccount(accountId, user, null, true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(accountRepository.findByIdAndUserId(accountId, userId))
                .thenReturn(Optional.of(account));
        when(financialTransactionRepository.findByAccountIdAndUserId(accountId, userId))
                .thenReturn(List.of());

        BigDecimal result = accountService.getAccountBalance(userId, accountId);

        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void getAccountBalance_shouldThrowWhenAccountDoesNotExist() {
        Long userId = 1L;
        Long accountId = 999L;

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(createUser(userId)));
        when(accountRepository.findByIdAndUserId(accountId, userId))
                .thenReturn(Optional.empty());

        assertThrows(
                AccountNotFoundException.class,
                () -> accountService.getAccountBalance(userId, accountId)
        );

        verify(financialTransactionRepository, never())
                .findByAccountIdAndUserId(accountId, userId);
    }

    @Test
    void getArchivedAccounts_shouldReturnInactiveAccounts() {
        Long userId = 1L;
        User user = createUser(userId);
        Account archivedAccount = createAccount(10L, user, BigDecimal.ZERO, false);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(accountRepository.findByUserIdAndActiveFalse(userId))
                .thenReturn(List.of(archivedAccount));

        List<AccountDto> result = accountService.getArchivedAccounts(userId);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getId());
        assertFalse(result.get(0).getActive());
        verify(accountRepository).findByUserIdAndActiveFalse(userId);
    }

    @Test
    void restoreAccount_shouldSetActiveToTrue() {
        Long userId = 1L;
        Long accountId = 10L;
        User user = createUser(userId);
        Account account = createAccount(accountId, user, BigDecimal.ZERO, false);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(accountRepository.findByIdAndUserId(accountId, userId))
                .thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);

        AccountDto result = accountService.restoreAccount(userId, accountId);

        assertTrue(result.getActive());
        assertTrue(account.getActive());
        verify(accountRepository).save(account);
    }

    @Test
    void restoreAccount_shouldThrowWhenAccountDoesNotExist() {
        Long userId = 1L;
        Long accountId = 999L;

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(createUser(userId)));
        when(accountRepository.findByIdAndUserId(accountId, userId))
                .thenReturn(Optional.empty());

        assertThrows(
                AccountNotFoundException.class,
                () -> accountService.restoreAccount(userId, accountId)
        );

        verify(accountRepository, never()).save(any(Account.class));
    }

    private User createUser(Long userId) {
        User user = new User();
        user.setId(userId);
        return user;
    }

    private Account createAccount(
            Long accountId,
            User user,
            BigDecimal initialBalance,
            Boolean active
    ) {
        Account account = new Account();
        account.setId(accountId);
        account.setName("Cash wallet");
        account.setType(AccountType.CASH);
        account.setInitialBalance(initialBalance);
        account.setCurrency(CurrencyType.VND);
        account.setActive(active);
        account.setUser(user);
        return account;
    }

    private AccountDto createAccountDto(
            BigDecimal initialBalance,
            Boolean active
    ) {
        return new AccountDto(
                null,
                "Cash wallet",
                AccountType.CASH,
                initialBalance,
                CurrencyType.VND,
                active,
                null
        );
    }
}
