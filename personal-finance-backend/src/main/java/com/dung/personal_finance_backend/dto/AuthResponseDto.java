package com.dung.personal_finance_backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponseDto {
    private String accessToken;
    private String tokenType;
    private long expiresIn;
    private UserDto user;
    private String recoveryCode;

    public AuthResponseDto(String accessToken, String tokenType, long expiresIn, UserDto user) {
        this(accessToken, tokenType, expiresIn, user, null);
    }

    public AuthResponseDto(
            String accessToken,
            String tokenType,
            long expiresIn,
            UserDto user,
            String recoveryCode
    ) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.user = user;
        this.recoveryCode = recoveryCode;
    }
}
