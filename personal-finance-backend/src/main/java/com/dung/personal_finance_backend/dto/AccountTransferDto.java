package com.dung.personal_finance_backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class AccountTransferDto {
    private Long id;

    @NotNull(message = "Source account is required")
    private Long sourceAccountId;

    @NotNull(message = "Target account is required")
    private Long targetAccountId;

    @NotNull(message = "Transfer amount is required")
    @DecimalMin(value = "0.01", message = "Transfer amount must be greater than 0")
    @Digits(integer = 17, fraction = 2, message = "Transfer amount may have at most 2 decimal places")
    private BigDecimal amount;

    @NotNull(message = "Transfer date is required")
    private LocalDate transferDate;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    private Instant createdAt;
}
