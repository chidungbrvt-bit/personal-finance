package com.dung.personal_finance_backend.mapper;

import com.dung.personal_finance_backend.dto.AccountDto;
import com.dung.personal_finance_backend.entity.Account;

public class AccountMapper {
    public static AccountDto toDto(Account account) {
        return new AccountDto(
                account.getId(),
                account.getName(),
                account.getType(),
                account.getInitialBalance(),
                account.getCurrency(),
                account.getActive(),
                account.getCreatedAt()
        );
    }

    public static Account toEntity(AccountDto accountDto) {
        return new Account(
                null,
                accountDto.getName(),
                accountDto.getType(),
                accountDto.getInitialBalance(),
                accountDto.getCurrency(),
                accountDto.getActive(),
                null,
                null,
                null
        );
    }
}
