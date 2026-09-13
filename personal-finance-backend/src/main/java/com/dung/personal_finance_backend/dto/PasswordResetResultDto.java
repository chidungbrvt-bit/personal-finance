package com.dung.personal_finance_backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PasswordResetResultDto(String recoveryCode) {
}
