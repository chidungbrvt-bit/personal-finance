package com.dung.personal_finance_backend.service;

import com.dung.personal_finance_backend.dto.AccountDto;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {
    public AccountDto createAccount(Long userId, AccountDto accountDto);
    public List<AccountDto> getAccountsByUserId(Long userId);
    public AccountDto getAccountById(Long userId, Long accountId);
    public AccountDto updateAccount(
            Long userId,
            Long accountId,
            AccountDto accountDto
    );
    public void deleteAccount(Long userId, Long accountId);
    public BigDecimal getAccountBalance(Long userId, Long accountId);
    public List<AccountDto> getArchivedAccounts(Long userId);
    public AccountDto restoreAccount(Long userId, Long accountId);
}
