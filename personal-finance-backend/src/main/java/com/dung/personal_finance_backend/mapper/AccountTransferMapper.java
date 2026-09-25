package com.dung.personal_finance_backend.mapper;

import com.dung.personal_finance_backend.dto.AccountTransferDto;
import com.dung.personal_finance_backend.entity.AccountTransfer;

public final class AccountTransferMapper {
    private AccountTransferMapper() {}

    public static AccountTransferDto toDto(AccountTransfer transfer) {
        AccountTransferDto dto = new AccountTransferDto();
        dto.setId(transfer.getId());
        dto.setSourceAccountId(transfer.getSourceAccount().getId());
        dto.setTargetAccountId(transfer.getTargetAccount().getId());
        dto.setAmount(transfer.getAmount());
        dto.setTransferDate(transfer.getTransferDate());
        dto.setDescription(transfer.getDescription());
        dto.setCreatedAt(transfer.getCreatedAt());
        return dto;
    }
}
