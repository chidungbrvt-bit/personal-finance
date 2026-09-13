package com.dung.personal_finance_backend.service;

import com.dung.personal_finance_backend.dto.PasswordResetResultDto;
import com.dung.personal_finance_backend.dto.request.ResetPasswordRequest;

public interface PasswordResetService {
    PasswordResetResultDto resetPassword(ResetPasswordRequest request);
}
