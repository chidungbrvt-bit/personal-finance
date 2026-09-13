package com.dung.personal_finance_backend.repository;

import com.dung.personal_finance_backend.entity.FinancialTransaction;
import com.dung.personal_finance_backend.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, Long> {
    List<FinancialTransaction> findByUserId(Long userId);
    List<FinancialTransaction> findByAccountIdAndUserId(Long accountId, Long userId);
    List<FinancialTransaction> findByCategoryIdAndUserId(Long categoryId, Long userId);
    Optional<FinancialTransaction> findByIdAndUserId(Long transactionId, Long userId);
    Boolean existsByAccountIdAndUserId(Long accountId, Long userId);
    Boolean existsByCategoryIdAndUserId(Long categoryId, Long userId);
    List<FinancialTransaction> findByUserIdAndTransactionDateGreaterThanEqualAndTransactionDateLessThan(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );
    List<FinancialTransaction>
    findByAccountIdAndUserIdAndTransactionDateGreaterThanEqualAndTransactionDateLessThan(
            Long accountId,
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );
    List<FinancialTransaction>
    findByCategoryIdAndUserIdAndTransactionDateGreaterThanEqualAndTransactionDateLessThan(
            Long categoryId,
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );
    List<FinancialTransaction>
    findByUserIdAndTypeAndTransactionDateGreaterThanEqualAndTransactionDateLessThan(
            Long userId,
            TransactionType type,
            LocalDate startDate,
            LocalDate endDate
    );
    List<FinancialTransaction> findByUserIdAndType(
            Long userId,
            TransactionType type
    );
}
