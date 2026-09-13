package com.dung.personal_finance_backend.service;

import com.dung.personal_finance_backend.dto.request.ResetPasswordRequest;

public interface PasswordResetService {
    void requestReset(String email);
    void resetPassword(ResetPasswordRequest request);
}
