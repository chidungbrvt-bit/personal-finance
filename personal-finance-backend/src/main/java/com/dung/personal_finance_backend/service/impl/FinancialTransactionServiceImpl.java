package com.dung.personal_finance_backend.service.impl;

import com.dung.personal_finance_backend.dto.FinancialTransactionDto;
import com.dung.personal_finance_backend.entity.Account;
import com.dung.personal_finance_backend.entity.Category;
import com.dung.personal_finance_backend.entity.FinancialTransaction;
import com.dung.personal_finance_backend.entity.User;
import com.dung.personal_finance_backend.enums.TransactionType;
import com.dung.personal_finance_backend.exception.*;
import com.dung.personal_finance_backend.mapper.FinancialTransactionMapper;
import com.dung.personal_finance_backend.repository.AccountRepository;
import com.dung.personal_finance_backend.repository.AccountTransferRepository;
import com.dung.personal_finance_backend.repository.CategoryRepository;
import com.dung.personal_finance_backend.repository.FinancialTransactionRepository;
import com.dung.personal_finance_backend.repository.UserRepository;
import com.dung.personal_finance_backend.service.FinancialTransactionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class FinancialTransactionServiceImpl implements FinancialTransactionService {
    private final FinancialTransactionRepository financialTransactionRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private  final CategoryRepository categoryRepository;
    private final AccountTransferRepository accountTransferRepository;

    public FinancialTransactionServiceImpl(FinancialTransactionRepository financialTransactionRepository,
                                           UserRepository userRepository,
                                           AccountRepository accountRepository,
                                           CategoryRepository categoryRepository,
                                           AccountTransferRepository accountTransferRepository) {
        this.financialTransactionRepository = financialTransactionRepository;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.accountTransferRepository = accountTransferRepository;
    }

    @Override
    @Transactional
    public FinancialTransactionDto createFinancialTransaction(
            Long userId,
            FinancialTransactionDto financialTransactionDto
    ){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        Account account = accountRepository
                .findByIdAndUserId(financialTransactionDto.getAccountId(), userId)
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Account not found with id: " + financialTransactionDto.getAccountId()
                        )
                );
        if (!Boolean.TRUE.equals(account.getActive())) {
            throw new BusinessRuleException(
                    "Cannot use an inactive account"
            );
        }
        Category category = categoryRepository
                .findByIdAndUserId(financialTransactionDto.getCategoryId(), userId)
                .orElseThrow(() ->
                        new CategoryNotFoundException(
                                "Category not found with id: " + financialTransactionDto.getCategoryId()
                        )
                );
        if (!Boolean.TRUE.equals(category.getActive())) {
            throw new BusinessRuleException(
                    "Cannot use an inactive category"
            );
        }
        if (financialTransactionDto.getAmount() == null ||
                financialTransactionDto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException(
                    "Transaction amount must be greater than 0"
            );
        }

        if (category.getType() != financialTransactionDto.getType()) {
            throw new BusinessRuleException(
                    "Category type must match transaction type"
            );
        }
        validateSufficientBalance(
                account,
                userId,
                financialTransactionDto.getType(),
                financialTransactionDto.getAmount(),
                null
        );
        FinancialTransaction financialTransaction = FinancialTransactionMapper.toEntity(financialTransactionDto);
        financialTransaction.setUser(user);
        financialTransaction.setAccount(account);
        financialTransaction.setCategory(category);
        FinancialTransaction savedFinancialTransaction = financialTransactionRepository.save(financialTransaction);
        return FinancialTransactionMapper.toDto(savedFinancialTransaction);
    }

    @Override
    public List<FinancialTransactionDto> getFinancialTransactionsByUserId(Long userId){
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        List<FinancialTransaction> financialTransactions = financialTransactionRepository.findByUserId(userId);
        List<FinancialTransactionDto> financialTransactionDtos = new ArrayList<>();
        for (FinancialTransaction financialTransaction : financialTransactions) {
            financialTransactionDtos.add(FinancialTransactionMapper.toDto(financialTransaction));
        }
        return financialTransactionDtos;
    }

    @Override
    public List<FinancialTransactionDto> getFinancialTransactionsByAccountId(
            Long userId,
            Long accountId
    ){
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account not found with id: " + accountId
                ));
        List<FinancialTransaction> financialTransactions = financialTransactionRepository.findByAccountIdAndUserId(
                accountId, userId
        );
        List<FinancialTransactionDto> financialTransactionDtos = new ArrayList<>();
        for (FinancialTransaction financialTransaction : financialTransactions) {
            financialTransactionDtos.add(FinancialTransactionMapper.toDto(financialTransaction));
        }
        return financialTransactionDtos;
    }

    @Override
    public List<FinancialTransactionDto> getFinancialTransactionsByCategoryId(
            Long userId,
            Long categoryId
    ){
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new CategoryNotFoundException(
                        "Category not found with id: " + categoryId
                ));
        List<FinancialTransaction> financialTransactions = financialTransactionRepository.findByCategoryIdAndUserId(
                categoryId, userId
        );
        List<FinancialTransactionDto> financialTransactionDtos = new ArrayList<>();
        for (FinancialTransaction financialTransaction : financialTransactions) {
            financialTransactionDtos.add(FinancialTransactionMapper.toDto(financialTransaction));
        }
        return financialTransactionDtos;
    }

    @Override
    public FinancialTransactionDto getFinancialTransactionById(
            Long userId,
            Long transactionId
    ) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with id: " + userId
                ));

        FinancialTransaction financialTransaction =
                financialTransactionRepository.findByIdAndUserId(transactionId, userId)
                        .orElseThrow(() -> new FinancialTransactionNotFoundException(
                                "Financial transaction not found with id: " + transactionId
                        ));

        return FinancialTransactionMapper.toDto(financialTransaction);
    }

    @Override
    @Transactional
    public FinancialTransactionDto updateFinancialTransaction(
            Long userId,
            Long transactionId,
            FinancialTransactionDto financialTransactionDto
    ){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        FinancialTransaction financialTransaction =
                financialTransactionRepository.findByIdAndUserId(
                        transactionId,
                        userId
                ).orElseThrow(() -> new FinancialTransactionNotFoundException(
                        "Financial transaction not found with id: " + transactionId
                ));
        Account account = accountRepository.findByIdAndUserId(
                financialTransactionDto.getAccountId(),
                userId
        ).orElseThrow(() -> new AccountNotFoundException(
                "Account not found with id: "
                        + financialTransactionDto.getAccountId()
        ));

        if (!Boolean.TRUE.equals(account.getActive())) {
            throw new BusinessRuleException(
                    "Cannot use an inactive account"
            );
        }

        Category category = categoryRepository.findByIdAndUserId(
                financialTransactionDto.getCategoryId(),
                userId
        ).orElseThrow(() -> new CategoryNotFoundException(
                "Category not found with id: "
                        + financialTransactionDto.getCategoryId()
        ));

        if (!Boolean.TRUE.equals(category.getActive())) {
            throw new BusinessRuleException(
                    "Cannot use an inactive category"
            );
        }

        if (financialTransactionDto.getAmount() == null ||
                financialTransactionDto.getAmount()
                        .compareTo(BigDecimal.ZERO) <= 0) {

            throw new BusinessRuleException(
                    "Transaction amount must be greater than 0"
            );
        }

        if (category.getType() != financialTransactionDto.getType()) {
            throw new BusinessRuleException(
                    "Category type must match financial transaction type"
            );
        }

        validateSufficientBalance(
                account,
                userId,
                financialTransactionDto.getType(),
                financialTransactionDto.getAmount(),
                financialTransaction
        );

        financialTransaction.setType(financialTransactionDto.getType());
        financialTransaction.setAmount(financialTransactionDto.getAmount());
        financialTransaction.setTransactionDate(financialTransactionDto.getTransactionDate());
        financialTransaction.setDescription(financialTransactionDto.getDescription());
        financialTransaction.setAccount(account);
        financialTransaction.setCategory(category);
        financialTransaction.setUser(user);
        FinancialTransaction updatedFinancialTransaction =
                financialTransactionRepository.save(financialTransaction);
        return FinancialTransactionMapper.toDto(updatedFinancialTransaction);
    }

    @Override
    public void deleteFinancialTransaction(
            Long userId,
            Long transactionId
    ){
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with id: " + userId
                ));

        FinancialTransaction financialTransaction =
                financialTransactionRepository.findByIdAndUserId(
                        transactionId,
                        userId
                ).orElseThrow(() -> new FinancialTransactionNotFoundException(
                        "Financial transaction not found with id: " + transactionId
                ));

        financialTransactionRepository.delete(financialTransaction);
    }

    @Override
    public List<FinancialTransactionDto> getFinancialTransactionsByDateRange(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    ){
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        List<FinancialTransaction> financialTransactions = financialTransactionRepository
                .findByUserIdAndTransactionDateGreaterThanEqualAndTransactionDateLessThan(
                        userId,
                        startDate,
                        endDate
                );
        List<FinancialTransactionDto> financialTransactionDtos = new ArrayList<>();
        for (FinancialTransaction financialTransaction : financialTransactions) {
            financialTransactionDtos.add(FinancialTransactionMapper.toDto(financialTransaction));
        }
        return financialTransactionDtos;
    }

    @Override
    public List<FinancialTransactionDto> getFinancialTransactionsByAccountAndDateRange(
            Long userId,
            Long accountId,
            LocalDate startDate,
            LocalDate endDate
    ){
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with id: " + accountId));
        List<FinancialTransaction> financialTransactions = financialTransactionRepository.
                findByAccountIdAndUserIdAndTransactionDateGreaterThanEqualAndTransactionDateLessThan(
                        accountId,
                        userId,
                        startDate,
                        endDate
                );
        List<FinancialTransactionDto> financialTransactionDtos = new ArrayList<>();
        for (FinancialTransaction financialTransaction : financialTransactions) {
            financialTransactionDtos.add(FinancialTransactionMapper.toDto(financialTransaction));
        }
        return financialTransactionDtos;
    }

    @Override
    public List<FinancialTransactionDto> getFinancialTransactionsByCategoryAndDateRange(
            Long userId,
            Long categoryId,
            LocalDate startDate,
            LocalDate endDate
    ){
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + categoryId));
        List<FinancialTransaction> financialTransactions = financialTransactionRepository.
                findByCategoryIdAndUserIdAndTransactionDateGreaterThanEqualAndTransactionDateLessThan(
                        categoryId,
                        userId,
                        startDate,
                        endDate
                );
        List<FinancialTransactionDto> financialTransactionDtos = new ArrayList<>();
        for (FinancialTransaction financialTransaction : financialTransactions) {
            financialTransactionDtos.add(FinancialTransactionMapper.toDto(financialTransaction));
        }
        return financialTransactionDtos;
    }

    @Override
    public List<FinancialTransactionDto> getFinancialTransactionsByTypeAndDateRange(
            Long userId,
            TransactionType type,
            LocalDate startDate,
            LocalDate endDate
    ){
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        List<FinancialTransaction> financialTransactions = financialTransactionRepository
                .findByUserIdAndTypeAndTransactionDateGreaterThanEqualAndTransactionDateLessThan(
                        userId,
                        type,
                        startDate,
                        endDate
                );
        List<FinancialTransactionDto> financialTransactionDtos = new ArrayList<>();
        for (FinancialTransaction financialTransaction : financialTransactions) {
            financialTransactionDtos.add(
                    FinancialTransactionMapper.toDto(financialTransaction)
            );
        }

        return financialTransactionDtos;
    }

    @Override
    public List<FinancialTransactionDto> getFinancialTransactionsByType(
            Long userId,
            TransactionType type
    ) {
        userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with id: " + userId
                        )
                );
        List<FinancialTransaction> financialTransactions =
                financialTransactionRepository.findByUserIdAndType(
                        userId,
                        type
                );
        List<FinancialTransactionDto> financialTransactionDtos = new ArrayList<>();
        for (FinancialTransaction financialTransaction : financialTransactions) {
            financialTransactionDtos.add(
                    FinancialTransactionMapper.toDto(financialTransaction)
            );
        }
        return financialTransactionDtos;
    }

    private void validateSufficientBalance(
            Account account,
            Long userId,
            TransactionType newType,
            BigDecimal newAmount,
            FinancialTransaction transactionBeingUpdated
    ) {
        if (newType != TransactionType.EXPENSE) {
            return;
        }

        BigDecimal availableBalance = calculateAccountBalance(account, userId);

        if (transactionBeingUpdated != null
                && transactionBeingUpdated.getAccount() != null
                && account.getId().equals(transactionBeingUpdated.getAccount().getId())) {
            if (transactionBeingUpdated.getType() == TransactionType.EXPENSE) {
                availableBalance = availableBalance.add(transactionBeingUpdated.getAmount());
            } else if (transactionBeingUpdated.getType() == TransactionType.INCOME) {
                availableBalance = availableBalance.subtract(transactionBeingUpdated.getAmount());
            }
        }

        if (newAmount.compareTo(availableBalance) > 0) {
            throw new BusinessRuleException("Insufficient account balance");
        }
    }

    private BigDecimal calculateAccountBalance(Account account, Long userId) {
        BigDecimal balance = account.getInitialBalance() != null
                ? account.getInitialBalance()
                : BigDecimal.ZERO;
        List<FinancialTransaction> transactions =
                financialTransactionRepository.findByAccountIdAndUserId(account.getId(), userId);

        if (transactions == null) {
            return balance;
        }

        for (FinancialTransaction transaction : transactions) {
            if (transaction.getType() == TransactionType.INCOME) {
                balance = balance.add(transaction.getAmount());
            } else if (transaction.getType() == TransactionType.EXPENSE) {
                balance = balance.subtract(transaction.getAmount());
            }
        }
        BigDecimal incoming = accountTransferRepository.sumIncoming(userId, account.getId());
        BigDecimal outgoing = accountTransferRepository.sumOutgoing(userId, account.getId());
        balance = balance
                .add(incoming == null ? BigDecimal.ZERO : incoming)
                .subtract(outgoing == null ? BigDecimal.ZERO : outgoing);
        return balance;
    }
}
