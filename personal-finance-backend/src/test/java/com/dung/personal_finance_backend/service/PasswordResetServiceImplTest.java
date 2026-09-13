package com.dung.personal_finance_backend.service;

import com.dung.personal_finance_backend.dto.PasswordResetResultDto;
import com.dung.personal_finance_backend.dto.request.ResetPasswordRequest;
import com.dung.personal_finance_backend.entity.User;
import com.dung.personal_finance_backend.exception.BusinessRuleException;
import com.dung.personal_finance_backend.repository.UserRepository;
import com.dung.personal_finance_backend.service.impl.PasswordResetServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceImplTest {
    private static final String LEGACY_CODE = "legacy-recovery-code-123456";

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RecoveryCodeService recoveryCodeService;

    private PasswordResetServiceImpl passwordResetService;
    private User user;

    @BeforeEach
    void setUp() {
        passwordResetService = new PasswordResetServiceImpl(
                userRepository,
                passwordEncoder,
                recoveryCodeService,
                LEGACY_CODE,
                "legacy@example.com"
        );
        user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setPassword("old-hash");
        user.setRecoveryCodeHash("personal-code-hash");
    }

    @Test
    void resetPasswordUsesTheRecoveryCodeBelongingToTheRequestedEmail() {
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(recoveryCodeService.matches("personal-recovery-code", "personal-code-hash")).thenReturn(true);
        when(passwordEncoder.encode("newPassword123")).thenReturn("new-password-hash");

        PasswordResetResultDto result = passwordResetService.resetPassword(
                new ResetPasswordRequest(" USER@EXAMPLE.COM ", " personal-recovery-code ", "newPassword123")
        );

        assertEquals("new-password-hash", user.getPassword());
        assertNull(result.recoveryCode());
        verify(userRepository).save(user);
    }

    @Test
    void resetPasswordRejectsARecoveryCodeFromAnotherUser() {
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(recoveryCodeService.matches("other-users-code", "personal-code-hash")).thenReturn(false);

        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> passwordResetService.resetPassword(
                        new ResetPasswordRequest("user@example.com", "other-users-code", "newPassword123")
                )
        );

        assertEquals("Email or recovery code is invalid", exception.getMessage());
        verify(passwordEncoder, never()).encode("newPassword123");
        verify(userRepository, never()).save(user);
    }

    @Test
    void resetPasswordDoesNotRevealWhetherEmailExists() {
        when(userRepository.findByEmailIgnoreCase("missing@example.com")).thenReturn(Optional.empty());

        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> passwordResetService.resetPassword(
                        new ResetPasswordRequest("missing@example.com", "some-recovery-code", "newPassword123")
                )
        );

        assertEquals("Email or recovery code is invalid", exception.getMessage());
    }

    @Test
    void legacyUserCanMigrateOnceAndReceivesANewPersonalCode() {
        user.setEmail("legacy@example.com");
        user.setRecoveryCodeHash(null);
        when(userRepository.findByEmailIgnoreCase("legacy@example.com")).thenReturn(Optional.of(user));
        when(recoveryCodeService.generate()).thenReturn("new-personal-recovery-code");
        when(recoveryCodeService.hash("new-personal-recovery-code")).thenReturn("new-code-hash");
        when(passwordEncoder.encode("newPassword123")).thenReturn("new-password-hash");

        PasswordResetResultDto result = passwordResetService.resetPassword(
                new ResetPasswordRequest("legacy@example.com", LEGACY_CODE, "newPassword123")
        );

        assertEquals("new-personal-recovery-code", result.recoveryCode());
        assertEquals("new-code-hash", user.getRecoveryCodeHash());
        verify(userRepository).save(user);
    }
}
