package com.dung.personal_finance_backend.service;

import com.dung.personal_finance_backend.dto.AuthResponseDto;
import com.dung.personal_finance_backend.dto.request.LoginRequest;
import com.dung.personal_finance_backend.dto.request.RegisterRequest;

public interface AuthService {
    AuthResponseDto register(RegisterRequest request);
    AuthResponseDto login(LoginRequest request);
}
