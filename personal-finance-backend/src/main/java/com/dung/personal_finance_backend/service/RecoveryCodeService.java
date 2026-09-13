package com.dung.personal_finance_backend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Service
public class RecoveryCodeService {
    private static final int RECOVERY_CODE_BYTES = 24;

    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public RecoveryCodeService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public String generate() {
        byte[] bytes = new byte[RECOVERY_CODE_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String hash(String recoveryCode) {
        return passwordEncoder.encode(recoveryCode);
    }

    public boolean matches(String recoveryCode, String recoveryCodeHash) {
        return passwordEncoder.matches(recoveryCode, recoveryCodeHash);
    }
}
