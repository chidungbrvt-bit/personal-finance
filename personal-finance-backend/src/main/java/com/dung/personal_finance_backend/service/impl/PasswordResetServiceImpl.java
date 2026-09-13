package com.dung.personal_finance_backend.service.impl;

import com.dung.personal_finance_backend.dto.request.ResetPasswordRequest;
import com.dung.personal_finance_backend.entity.PasswordResetToken;
import com.dung.personal_finance_backend.entity.User;
import com.dung.personal_finance_backend.exception.BusinessRuleException;
import com.dung.personal_finance_backend.repository.PasswordResetTokenRepository;
import com.dung.personal_finance_backend.repository.UserRepository;
import com.dung.personal_finance_backend.service.PasswordResetEmailService;
import com.dung.personal_finance_backend.service.PasswordResetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {
    private static final Logger log = LoggerFactory.getLogger(PasswordResetServiceImpl.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetEmailService emailService;
    private final String frontendUrl;
    private final long expirationMinutes;

    public PasswordResetServiceImpl(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            PasswordResetEmailService emailService,
            @Value("${app.frontend-url:http://localhost:5173}") String frontendUrl,
            @Value("${app.password-reset.expiration-minutes:15}") long expirationMinutes
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.frontendUrl = frontendUrl.replaceAll("/+$", "");
        this.expirationMinutes = expirationMinutes;
    }

    @Override
    @Transactional
    public void requestReset(String email) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        User user = userRepository.findByEmailIgnoreCase(normalizedEmail).orElse(null);
        if (user == null) {
            return;
        }

        tokenRepository.deleteByUserId(user.getId());
        String rawToken = generateToken();
        PasswordResetToken resetToken = new PasswordResetToken(
                hashToken(rawToken),
                user,
                Instant.now().plus(expirationMinutes, ChronoUnit.MINUTES)
        );
        tokenRepository.save(resetToken);

        String resetLink = UriComponentsBuilder.fromUriString(frontendUrl)
                .path("/reset-password")
                .queryParam("token", rawToken)
                .build()
                .toUriString();
        try {
            emailService.sendResetLink(user.getEmail(), resetLink, expirationMinutes);
        } catch (MailException exception) {
            tokenRepository.delete(resetToken);
            log.error("Unable to send password reset email", exception);
        }
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = tokenRepository.findByTokenHash(hashToken(request.getToken()))
                .orElseThrow(this::invalidToken);
        if (!resetToken.getExpiresAt().isAfter(Instant.now())) {
            tokenRepository.delete(resetToken);
            throw invalidToken();
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        tokenRepository.deleteByUserId(user.getId());
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private BusinessRuleException invalidToken() {
        return new BusinessRuleException("Password reset link is invalid or expired");
    }
}
