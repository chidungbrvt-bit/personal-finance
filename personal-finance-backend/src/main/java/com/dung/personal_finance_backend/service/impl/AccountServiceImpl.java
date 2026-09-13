package com.dung.personal_finance_backend.service.impl;

import com.dung.personal_finance_backend.dto.AccountDto;
import com.dung.personal_finance_backend.entity.Account;
import com.dung.personal_finance_backend.entity.FinancialTransaction;
import com.dung.personal_finance_backend.entity.User;
import com.dung.personal_finance_backend.enums.CurrencyType;
import com.dung.personal_finance_backend.enums.TransactionType;
import com.dung.personal_finance_backend.exception.AccountNotFoundException;
import com.dung.personal_finance_backend.exception.BusinessRuleException;
import com.dung.personal_finance_backend.exception.UserNotFoundException;
import com.dung.personal_finance_backend.mapper.AccountMapper;
import com.dung.personal_finance_backend.repository.AccountRepository;
import com.dung.personal_finance_backend.repository.FinancialTransactionRepository;
import com.dung.personal_finance_backend.repository.UserRepository;
import com.dung.personal_finance_backend.service.AccountService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final FinancialTransactionRepository financialTransactionRepository;

    public AccountServiceImpl(
            AccountRepository accountRepository,
            UserRepository userRepository,
            FinancialTransactionRepository financialTransactionRepository
    ) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.financialTransactionRepository = financialTransactionRepository;
    }

    @Override
    public AccountDto createAccount(Long userId, AccountDto accountDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        if (accountDto.getInitialBalance() == null) {
            throw new BusinessRuleException(
                    "Initial balance is required"
            );
        }
        validateCurrency(accountDto.getCurrency());
        Account account = AccountMapper.toEntity(accountDto);
        if (account.getActive() == null) {
            account.setActive(true);
        }
        account.setUser(user);
        Account savedAccount = accountRepository.save(account);
        return AccountMapper.toDto(savedAccount);
    }

    @Override
    public List<AccountDto> getAccountsByUserId(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        List<Account> accounts = accountRepository.findByUserIdAndActiveTrue(userId);
        List<AccountDto> accountDtos = new ArrayList<>();
        for (Account account : accounts) {
            accountDtos.add(AccountMapper.toDto(account));
        }
        return accountDtos;
    }

    @Override
    public AccountDto getAccountById(Long userId, Long accountId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        Account account = accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with id: " + accountId));
        return AccountMapper.toDto(account);
    }

    @Override
    public AccountDto updateAccount(
            Long userId,
            Long accountId,
            AccountDto accountDto
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        Account account = accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with id: " + accountId));
        validateCurrency(accountDto.getCurrency());
        account.setName(accountDto.getName());
        account.setType(accountDto.getType());
        account.setInitialBalance(accountDto.getInitialBalance());
        account.setCurrency(accountDto.getCurrency());
        if(accountDto.getActive() != null) {
            account.setActive(accountDto.getActive());
        }
        Account updatedAccount = accountRepository.save(account);
        return AccountMapper.toDto(updatedAccount);
    }

    @Override
    public void deleteAccount(Long userId, Long accountId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        Account account = accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with id: " + accountId));
        Boolean hasTransactions = financialTransactionRepository.existsByAccountIdAndUserId(accountId, userId);
        if (hasTransactions) {
            account.setActive(false);
            accountRepository.save(account);
        } else {
            accountRepository.delete(account);
        }
    }

    @Override
    public BigDecimal getAccountBalance(Long userId, Long accountId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        Account account = accountRepository.findByIdAndUserId(
                accountId,
                userId
        ).orElseThrow(() -> new AccountNotFoundException(
                "Account not found with id: " + accountId
        ));

        List<FinancialTransaction> financialTransactions =
                financialTransactionRepository.findByAccountIdAndUserId(
                        accountId,
                        userId
                );
        BigDecimal balance = account.getInitialBalance() != null
                ? account.getInitialBalance()
                : BigDecimal.ZERO;
        for (FinancialTransaction financialTransaction : financialTransactions) {
            if (financialTransaction.getType() == TransactionType.INCOME) {
                balance = balance.add(financialTransaction.getAmount());
            } else if (financialTransaction.getType() == TransactionType.EXPENSE) {
                balance = balance.subtract(financialTransaction.getAmount());
            }
        }
        return balance;
    }

    @Override
    public List<AccountDto> getArchivedAccounts(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        List<Account> accounts = accountRepository.findByUserIdAndActiveFalse(userId);
        List<AccountDto> accountDtos = new ArrayList<>();
        for(Account account : accounts) {
            accountDtos.add(AccountMapper.toDto(account));
        }
        return accountDtos;
    }

    @Override
    public AccountDto restoreAccount(Long userId, Long accountId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        Account account = accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with id: " + accountId));
        account.setActive(true);
        Account restoredAccount = accountRepository.save(account);
        return AccountMapper.toDto(restoredAccount);
    }

    private void validateCurrency(CurrencyType currency) {
        if (currency != CurrencyType.VND) {
            throw new BusinessRuleException(
                    "Only VND currency is supported"
            );
        }
    }
}
