package com.dung.personal_finance_backend.service;

import com.dung.personal_finance_backend.dto.FinancialTransactionDto;
import com.dung.personal_finance_backend.enums.TransactionType;

import java.time.LocalDate;
import java.util.List;

public interface FinancialTransactionService {
    FinancialTransactionDto createFinancialTransaction(
            Long userId,
            FinancialTransactionDto financialTransactionDto
    );
    List<FinancialTransactionDto> getFinancialTransactionsByUserId(Long userID);
    List<FinancialTransactionDto> getFinancialTransactionsByAccountId(
            Long userId,
            Long accountId
    );
    List<FinancialTransactionDto> getFinancialTransactionsByCategoryId(
            Long userId,
            Long categoryId
    );
    FinancialTransactionDto getFinancialTransactionById(
            Long userId,
            Long transactionId
    );
    FinancialTransactionDto updateFinancialTransaction(
            Long userId,
            Long transactionId,
            FinancialTransactionDto financialTransactionDto
    );
    void deleteFinancialTransaction(
            Long userId,
            Long transactionId
    );
    List<FinancialTransactionDto> getFinancialTransactionsByDateRange(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );
    List<FinancialTransactionDto> getFinancialTransactionsByAccountAndDateRange(
            Long userId,
            Long accountId,
            LocalDate startDate,
            LocalDate endDate
    );
    List<FinancialTransactionDto> getFinancialTransactionsByCategoryAndDateRange(
            Long userId,
            Long categoryId,
            LocalDate startDate,
            LocalDate endDate
    );
    List<FinancialTransactionDto> getFinancialTransactionsByTypeAndDateRange(
            Long userId,
            TransactionType type,
            LocalDate startDate,
            LocalDate endDate
    );
    List<FinancialTransactionDto> getFinancialTransactionsByType(
            Long userId,
            TransactionType type
    );
}
