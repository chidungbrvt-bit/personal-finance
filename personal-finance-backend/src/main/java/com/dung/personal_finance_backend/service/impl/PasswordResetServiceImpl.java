package com.dung.personal_finance_backend.service.impl;

import com.dung.personal_finance_backend.dto.request.ResetPasswordRequest;
import com.dung.personal_finance_backend.entity.User;
import com.dung.personal_finance_backend.exception.BusinessRuleException;
import com.dung.personal_finance_backend.repository.UserRepository;
import com.dung.personal_finance_backend.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String configuredRecoveryCode;

    public PasswordResetServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.password-reset.recovery-code:}") String configuredRecoveryCode
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.configuredRecoveryCode = configuredRecoveryCode.trim();
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (configuredRecoveryCode.isBlank()) {
            throw new BusinessRuleException("Password recovery is not configured");
        }
        if (!codesMatch(request.getRecoveryCode().trim(), configuredRecoveryCode)) {
            throw new BusinessRuleException("Recovery code is invalid");
        }

        List<User> users = userRepository.findAll();
        if (users.size() != 1) {
            throw new BusinessRuleException(
                    "Password recovery is available only when exactly one user exists"
            );
        }

        User user = users.getFirst();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private boolean codesMatch(String suppliedCode, String expectedCode) {
        return MessageDigest.isEqual(
                suppliedCode.getBytes(StandardCharsets.UTF_8),
                expectedCode.getBytes(StandardCharsets.UTF_8)
        );
    }
}
