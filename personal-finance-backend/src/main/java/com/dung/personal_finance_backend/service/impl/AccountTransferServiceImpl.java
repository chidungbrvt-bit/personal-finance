package com.dung.personal_finance_backend.service.impl;

import com.dung.personal_finance_backend.dto.AccountTransferDto;
import com.dung.personal_finance_backend.entity.Account;
import com.dung.personal_finance_backend.entity.AccountTransfer;
import com.dung.personal_finance_backend.entity.FinancialTransaction;
import com.dung.personal_finance_backend.entity.User;
import com.dung.personal_finance_backend.enums.TransactionType;
import com.dung.personal_finance_backend.exception.AccountNotFoundException;
import com.dung.personal_finance_backend.exception.BusinessRuleException;
import com.dung.personal_finance_backend.exception.UserNotFoundException;
import com.dung.personal_finance_backend.mapper.AccountTransferMapper;
import com.dung.personal_finance_backend.repository.AccountRepository;
import com.dung.personal_finance_backend.repository.AccountTransferRepository;
import com.dung.personal_finance_backend.repository.FinancialTransactionRepository;
import com.dung.personal_finance_backend.repository.UserRepository;
import com.dung.personal_finance_backend.service.AccountTransferService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountTransferServiceImpl implements AccountTransferService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final FinancialTransactionRepository transactionRepository;
    private final AccountTransferRepository transferRepository;

    public AccountTransferServiceImpl(
            UserRepository userRepository,
            AccountRepository accountRepository,
            FinancialTransactionRepository transactionRepository,
            AccountTransferRepository transferRepository
    ) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.transferRepository = transferRepository;
    }

    @Override
    @Transactional
    public AccountTransferDto createTransfer(Long userId, AccountTransferDto request) {
        validateRequest(request);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        if (request.getSourceAccountId().equals(request.getTargetAccountId())) {
            throw new BusinessRuleException("Source and target accounts must be different");
        }

        // Lock in a stable order so opposite simultaneous transfers do not deadlock.
        long firstId = Math.min(request.getSourceAccountId(), request.getTargetAccountId());
        long secondId = Math.max(request.getSourceAccountId(), request.getTargetAccountId());
        Account first = lockAccount(firstId, userId);
        Account second = lockAccount(secondId, userId);
        Account source = first.getId().equals(request.getSourceAccountId()) ? first : second;
        Account target = first.getId().equals(request.getTargetAccountId()) ? first : second;

        if (!Boolean.TRUE.equals(source.getActive()) || !Boolean.TRUE.equals(target.getActive())) {
            throw new BusinessRuleException("Cannot transfer using an inactive account");
        }
        if (source.getCurrency() != target.getCurrency()) {
            throw new BusinessRuleException("Source and target accounts must use the same currency");
        }

        BigDecimal available = calculateBalance(source, userId);
        if (request.getAmount().compareTo(available) > 0) {
            throw new BusinessRuleException("Insufficient account balance");
        }

        AccountTransfer transfer = new AccountTransfer();
        transfer.setUser(user);
        transfer.setSourceAccount(source);
        transfer.setTargetAccount(target);
        transfer.setAmount(request.getAmount());
        transfer.setTransferDate(request.getTransferDate());
        transfer.setDescription(normalizeDescription(request.getDescription()));
        return AccountTransferMapper.toDto(transferRepository.save(transfer));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountTransferDto> getTransfers(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        return transferRepository.findByUserIdOrderByTransferDateDescCreatedAtDesc(userId)
                .stream()
                .map(AccountTransferMapper::toDto)
                .toList();
    }

    private Account lockAccount(Long accountId, Long userId) {
        return accountRepository.findByIdAndUserIdForUpdate(accountId, userId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with id: " + accountId));
    }

    private void validateRequest(AccountTransferDto request) {
        if (request == null
                || request.getSourceAccountId() == null
                || request.getTargetAccountId() == null) {
            throw new BusinessRuleException("Source and target accounts are required");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Transfer amount must be greater than 0");
        }
        if (request.getAmount().scale() > 2) {
            throw new BusinessRuleException("Transfer amount may have at most 2 decimal places");
        }
        if (request.getTransferDate() == null) {
            throw new BusinessRuleException("Transfer date is required");
        }
    }

    private BigDecimal calculateBalance(Account account, Long userId) {
        BigDecimal balance = account.getInitialBalance() == null
                ? BigDecimal.ZERO
                : account.getInitialBalance();
        List<FinancialTransaction> transactions =
                transactionRepository.findByAccountIdAndUserId(account.getId(), userId);
        for (FinancialTransaction transaction : transactions) {
            balance = transaction.getType() == TransactionType.INCOME
                    ? balance.add(transaction.getAmount())
                    : balance.subtract(transaction.getAmount());
        }
        return balance
                .add(zeroIfNull(transferRepository.sumIncoming(userId, account.getId())))
                .subtract(zeroIfNull(transferRepository.sumOutgoing(userId, account.getId())));
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        return description.trim();
    }
}
