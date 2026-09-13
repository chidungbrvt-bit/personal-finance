package com.dung.personal_finance_backend.service;

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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceImplTest {
    private static final String RECOVERY_CODE = "private-recovery-code-123456";

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private PasswordResetServiceImpl passwordResetService;
    private User user;

    @BeforeEach
    void setUp() {
        passwordResetService = new PasswordResetServiceImpl(
                userRepository,
                passwordEncoder,
                RECOVERY_CODE
        );
        user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setPassword("old-hash");
    }

    @Test
    void resetPasswordReplacesTheOnlyUsersPassword() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(passwordEncoder.encode("newPassword123")).thenReturn("new-hash");

        passwordResetService.resetPassword(
                new ResetPasswordRequest(RECOVERY_CODE, "newPassword123")
        );

        assertEquals("new-hash", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void resetPasswordRejectsInvalidRecoveryCode() {
        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> passwordResetService.resetPassword(
                        new ResetPasswordRequest("wrong-recovery-code", "newPassword123")
                )
        );

        assertEquals("Recovery code is invalid", exception.getMessage());
        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void resetPasswordRejectsMissingServerConfiguration() {
        PasswordResetServiceImpl unconfiguredService = new PasswordResetServiceImpl(
                userRepository,
                passwordEncoder,
                ""
        );

        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> unconfiguredService.resetPassword(
                        new ResetPasswordRequest(RECOVERY_CODE, "newPassword123")
                )
        );

        assertEquals("Password recovery is not configured", exception.getMessage());
        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void resetPasswordRejectsDatabaseWithMoreThanOneUser() {
        User secondUser = new User();
        secondUser.setId(2L);
        when(userRepository.findAll()).thenReturn(List.of(user, secondUser));

        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> passwordResetService.resetPassword(
                        new ResetPasswordRequest(RECOVERY_CODE, "newPassword123")
                )
        );

        assertEquals(
                "Password recovery is available only when exactly one user exists",
                exception.getMessage()
        );
        verify(passwordEncoder, never()).encode("newPassword123");
        verify(userRepository, never()).save(user);
    }
}
