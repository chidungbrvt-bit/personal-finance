package com.dung.personal_finance_backend.mapper;

import com.dung.personal_finance_backend.dto.FinancialTransactionDto;
import com.dung.personal_finance_backend.entity.FinancialTransaction;

public class FinancialTransactionMapper {

    public static FinancialTransactionDto toDto(FinancialTransaction financialTransaction) {
        return new FinancialTransactionDto(
                financialTransaction.getId(),
                financialTransaction.getType(), 
                financialTransaction.getAmount(),
                financialTransaction.getTransactionDate(),
                financialTransaction.getDescription(),
                financialTransaction.getAccount() == null
                        ? null
                        : financialTransaction.getAccount().getId(),
                financialTransaction.getCategory() == null
                        ? null
                        : financialTransaction.getCategory().getId(),
                financialTransaction.getCreatedAt()
        );
    }

    public static FinancialTransaction toEntity(
            FinancialTransactionDto financialTransactionDto) {
        return new FinancialTransaction(
                null,
                financialTransactionDto.getType(),
                financialTransactionDto.getAmount(),
                financialTransactionDto.getTransactionDate(),
                financialTransactionDto.getDescription(),
                null,
                null,
                null,
                null,
                null
        );
    }
}
