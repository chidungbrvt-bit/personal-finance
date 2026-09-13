package com.dung.personal_finance_backend.service;

import com.dung.personal_finance_backend.dto.request.ResetPasswordRequest;
import com.dung.personal_finance_backend.entity.PasswordResetToken;
import com.dung.personal_finance_backend.entity.User;
import com.dung.personal_finance_backend.exception.BusinessRuleException;
import com.dung.personal_finance_backend.repository.PasswordResetTokenRepository;
import com.dung.personal_finance_backend.repository.UserRepository;
import com.dung.personal_finance_backend.service.impl.PasswordResetServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordResetTokenRepository tokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private PasswordResetEmailService emailService;

    private PasswordResetServiceImpl passwordResetService;
    private User user;

    @BeforeEach
    void setUp() {
        passwordResetService = new PasswordResetServiceImpl(
                userRepository,
                tokenRepository,
                passwordEncoder,
                emailService,
                "https://finance.example.com/",
                15
        );
        user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setPassword("old-hash");
    }

    @Test
    void requestResetStoresOnlyTokenHashAndSendsLink() {
        when(userRepository.findByEmailIgnoreCase("user@example.com"))
                .thenReturn(Optional.of(user));
        ArgumentCaptor<PasswordResetToken> tokenCaptor =
                ArgumentCaptor.forClass(PasswordResetToken.class);
        ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);

        passwordResetService.requestReset("  USER@EXAMPLE.COM  ");

        verify(tokenRepository).deleteByUserId(1L);
        verify(tokenRepository).save(tokenCaptor.capture());
        verify(emailService).sendResetLink(
                org.mockito.ArgumentMatchers.eq("user@example.com"),
                linkCaptor.capture(),
                org.mockito.ArgumentMatchers.eq(15L)
        );
        String resetLink = linkCaptor.getValue();
        String rawToken = resetLink.substring(resetLink.indexOf("token=") + 6);
        assertTrue(resetLink.startsWith("https://finance.example.com/reset-password?token="));
        assertNotEquals(rawToken, tokenCaptor.getValue().getTokenHash());
        assertEquals(sha256(rawToken), tokenCaptor.getValue().getTokenHash());
        assertTrue(tokenCaptor.getValue().getExpiresAt().isAfter(Instant.now()));
    }

    @Test
    void requestResetDoesNothingForUnknownEmail() {
        when(userRepository.findByEmailIgnoreCase("unknown@example.com"))
                .thenReturn(Optional.empty());

        passwordResetService.requestReset("unknown@example.com");

        verifyNoInteractions(tokenRepository, emailService);
    }

    @Test
    void requestResetDoesNotExposeMailFailureAndRemovesUnusableToken() {
        when(userRepository.findByEmailIgnoreCase("user@example.com"))
                .thenReturn(Optional.of(user));
        doThrow(new MailSendException("SMTP unavailable"))
                .when(emailService)
                .sendResetLink(
                        org.mockito.ArgumentMatchers.eq("user@example.com"),
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.eq(15L)
                );
        ArgumentCaptor<PasswordResetToken> tokenCaptor =
                ArgumentCaptor.forClass(PasswordResetToken.class);

        passwordResetService.requestReset("user@example.com");

        verify(tokenRepository).save(tokenCaptor.capture());
        verify(tokenRepository).delete(tokenCaptor.getValue());
    }

    @Test
    void resetPasswordReplacesHashAndConsumesToken() {
        String rawToken = "valid-reset-token";
        PasswordResetToken token = new PasswordResetToken(
                sha256(rawToken), user, Instant.now().plusSeconds(300));
        when(tokenRepository.findByTokenHash(sha256(rawToken)))
                .thenReturn(Optional.of(token));
        when(passwordEncoder.encode("newPassword123")).thenReturn("new-hash");

        passwordResetService.resetPassword(
                new ResetPasswordRequest(rawToken, "newPassword123"));

        assertEquals("new-hash", user.getPassword());
        verify(userRepository).save(user);
        verify(tokenRepository).deleteByUserId(1L);
    }

    @Test
    void resetPasswordRejectsExpiredToken() {
        String rawToken = "expired-reset-token";
        PasswordResetToken token = new PasswordResetToken(
                sha256(rawToken), user, Instant.now().minusSeconds(1));
        when(tokenRepository.findByTokenHash(sha256(rawToken)))
                .thenReturn(Optional.of(token));

        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> passwordResetService.resetPassword(
                        new ResetPasswordRequest(rawToken, "newPassword123"))
        );

        assertEquals("Password reset link is invalid or expired", exception.getMessage());
        verify(tokenRepository).delete(token);
        verify(userRepository, never()).save(any());
    }

    private String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
