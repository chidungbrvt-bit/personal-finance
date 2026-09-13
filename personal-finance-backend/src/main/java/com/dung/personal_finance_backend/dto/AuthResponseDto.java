package com.dung.personal_finance_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AuthResponseDto {
    private String accessToken;
    private String tokenType;
    private long expiresIn;
    private UserDto user;
}
