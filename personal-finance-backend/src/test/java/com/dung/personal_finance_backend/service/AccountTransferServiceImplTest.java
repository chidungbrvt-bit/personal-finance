package com.dung.personal_finance_backend.service;

import com.dung.personal_finance_backend.dto.AccountTransferDto;
import com.dung.personal_finance_backend.entity.Account;
import com.dung.personal_finance_backend.entity.AccountTransfer;
import com.dung.personal_finance_backend.entity.User;
import com.dung.personal_finance_backend.enums.CurrencyType;
import com.dung.personal_finance_backend.exception.BusinessRuleException;
import com.dung.personal_finance_backend.repository.AccountRepository;
import com.dung.personal_finance_backend.repository.AccountTransferRepository;
import com.dung.personal_finance_backend.repository.FinancialTransactionRepository;
import com.dung.personal_finance_backend.repository.UserRepository;
import com.dung.personal_finance_backend.service.impl.AccountTransferServiceImpl;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountTransferServiceImplTest {
    @Mock private UserRepository userRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private FinancialTransactionRepository transactionRepository;
    @Mock private AccountTransferRepository transferRepository;

    private AccountTransferServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AccountTransferServiceImpl(
                userRepository, accountRepository, transactionRepository, transferRepository
        );
    }

    @Test
    void createTransfer_shouldMoveMoneyBetweenOwnedAccounts() {
        User user = user(1L);
        Account source = account(10L, user, "1000.00");
        Account target = account(20L, user, "100.00");
        AccountTransferDto request = request(10L, 20L, "300.00");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(accountRepository.findByIdAndUserIdForUpdate(10L, 1L)).thenReturn(Optional.of(source));
        when(accountRepository.findByIdAndUserIdForUpdate(20L, 1L)).thenReturn(Optional.of(target));
        when(transactionRepository.findByAccountIdAndUserId(10L, 1L)).thenReturn(List.of());
        when(transferRepository.save(any(AccountTransfer.class))).thenAnswer(invocation -> {
            AccountTransfer transfer = invocation.getArgument(0);
            transfer.setId(99L);
            return transfer;
        });

        AccountTransferDto result = service.createTransfer(1L, request);

        assertEquals(99L, result.getId());
        assertEquals(10L, result.getSourceAccountId());
        assertEquals(20L, result.getTargetAccountId());
        assertEquals(new BigDecimal("300.00"), result.getAmount());
        verify(transferRepository).save(any(AccountTransfer.class));
    }

    @Test
    void createTransfer_shouldRejectInsufficientBalance() {
        User user = user(1L);
        Account source = account(10L, user, "100.00");
        Account target = account(20L, user, "100.00");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(accountRepository.findByIdAndUserIdForUpdate(10L, 1L)).thenReturn(Optional.of(source));
        when(accountRepository.findByIdAndUserIdForUpdate(20L, 1L)).thenReturn(Optional.of(target));
        when(transactionRepository.findByAccountIdAndUserId(10L, 1L)).thenReturn(List.of());

        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> service.createTransfer(1L, request(10L, 20L, "101.00"))
        );

        assertEquals("Insufficient account balance", exception.getMessage());
        verify(transferRepository, never()).save(any(AccountTransfer.class));
    }

    @Test
    void createTransfer_shouldRejectSameAccount() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));

        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> service.createTransfer(1L, request(10L, 10L, "1.00"))
        );

        assertEquals("Source and target accounts must be different", exception.getMessage());
        verify(accountRepository, never()).findByIdAndUserIdForUpdate(any(), any());
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    private Account account(Long id, User user, String balance) {
        Account account = new Account();
        account.setId(id);
        account.setUser(user);
        account.setInitialBalance(new BigDecimal(balance));
        account.setCurrency(CurrencyType.VND);
        account.setActive(true);
        return account;
    }

    private AccountTransferDto request(Long sourceId, Long targetId, String amount) {
        AccountTransferDto dto = new AccountTransferDto();
        dto.setSourceAccountId(sourceId);
        dto.setTargetAccountId(targetId);
        dto.setAmount(new BigDecimal(amount));
        dto.setTransferDate(LocalDate.of(2026, 9, 21));
        return dto;
    }
}
