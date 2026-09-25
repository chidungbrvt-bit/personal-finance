package com.dung.personal_finance_backend.service;

import com.dung.personal_finance_backend.dto.AccountTransferDto;

import java.util.List;

public interface AccountTransferService {
    AccountTransferDto createTransfer(Long userId, AccountTransferDto request);
    List<AccountTransferDto> getTransfers(Long userId);
}
