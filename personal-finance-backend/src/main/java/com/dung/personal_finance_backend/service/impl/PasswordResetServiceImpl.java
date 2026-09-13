package com.dung.personal_finance_backend.service.impl;

import com.dung.personal_finance_backend.dto.PasswordResetResultDto;
import com.dung.personal_finance_backend.dto.request.ResetPasswordRequest;
import com.dung.personal_finance_backend.entity.User;
import com.dung.personal_finance_backend.exception.BusinessRuleException;
import com.dung.personal_finance_backend.repository.UserRepository;
import com.dung.personal_finance_backend.service.PasswordResetService;
import com.dung.personal_finance_backend.service.RecoveryCodeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RecoveryCodeService recoveryCodeService;
    private final String configuredRecoveryCode;
    private final String configuredUserEmail;

    public PasswordResetServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            RecoveryCodeService recoveryCodeService,
            @Value("${app.password-reset.recovery-code:}") String configuredRecoveryCode,
            @Value("${app.password-reset.user-email:}") String configuredUserEmail
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.recoveryCodeService = recoveryCodeService;
        this.configuredRecoveryCode = configuredRecoveryCode.trim();
        this.configuredUserEmail = configuredUserEmail.trim().toLowerCase(Locale.ROOT);
    }

    @Override
    @Transactional
    public PasswordResetResultDto resetPassword(ResetPasswordRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase(Locale.ROOT);
        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(this::invalidCredentials);

        String suppliedCode = request.getRecoveryCode().trim();
        String newRecoveryCode = null;
        if (user.getRecoveryCodeHash() != null && !user.getRecoveryCodeHash().isBlank()) {
            if (!recoveryCodeService.matches(suppliedCode, user.getRecoveryCodeHash())) {
                throw invalidCredentials();
            }
        } else {
            if (!canUseLegacyRecovery(normalizedEmail, suppliedCode)) {
                throw invalidCredentials();
            }
            newRecoveryCode = recoveryCodeService.generate();
            user.setRecoveryCodeHash(recoveryCodeService.hash(newRecoveryCode));
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return new PasswordResetResultDto(newRecoveryCode);
    }

    private boolean canUseLegacyRecovery(String normalizedEmail, String suppliedCode) {
        return !configuredRecoveryCode.isBlank()
                && !configuredUserEmail.isBlank()
                && normalizedEmail.equals(configuredUserEmail)
                && codesMatch(suppliedCode, configuredRecoveryCode);
    }

    private BusinessRuleException invalidCredentials() {
        return new BusinessRuleException("Email or recovery code is invalid");
    }

    private boolean codesMatch(String suppliedCode, String expectedCode) {
        return MessageDigest.isEqual(
                suppliedCode.getBytes(StandardCharsets.UTF_8),
                expectedCode.getBytes(StandardCharsets.UTF_8)
        );
    }
}
