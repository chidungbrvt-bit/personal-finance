package com.dung.personal_finance_backend.dto;

import com.dung.personal_finance_backend.enums.AccountType;
import com.dung.personal_finance_backend.enums.CurrencyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AccountDto {
    private Long id;

    @NotBlank(message = "Account name is required")
    private String name;

    @NotNull(message = "Account type is required")
    private AccountType type;

    @NotNull(message = "Initial balance is required")
    private BigDecimal initialBalance;

    @NotNull(message = "Currency is required")
    private CurrencyType currency;

    private Boolean active;
    private Instant createdAt;
}
