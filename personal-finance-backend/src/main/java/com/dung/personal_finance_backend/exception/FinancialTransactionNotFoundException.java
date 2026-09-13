package com.dung.personal_finance_backend.exception;

public class FinancialTransactionNotFoundException extends RuntimeException{
    public FinancialTransactionNotFoundException(String message) {
        super(message);
    }
}
