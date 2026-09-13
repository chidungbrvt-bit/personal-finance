package com.dung.personal_finance_backend.controller;

import com.dung.personal_finance_backend.dto.AuthResponseDto;
import com.dung.personal_finance_backend.dto.PasswordResetResultDto;
import com.dung.personal_finance_backend.dto.UserDto;
import com.dung.personal_finance_backend.dto.request.LoginRequest;
import com.dung.personal_finance_backend.dto.request.RegisterRequest;
import com.dung.personal_finance_backend.enums.Role;
import com.dung.personal_finance_backend.exception.GlobalExceptionHandler;
import com.dung.personal_finance_backend.service.AuthService;
import com.dung.personal_finance_backend.service.PasswordResetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
    @Mock
    private AuthService authService;
    @Mock
    private PasswordResetService passwordResetService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(authService, passwordResetService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void registerReturnsCreatedAndToken() throws Exception {
        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Dung",
                                  "email":"dung@example.com",
                                  "password":"password123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.data.accessToken").value("test-token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.recoveryCode").value("personal-recovery-code"))
                .andExpect(jsonPath("$.data.user.role").value("USER"));

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    void registerRejectsInvalidBodyBeforeCallingService() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","email":"invalid","password":"short"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.data.name").exists())
                .andExpect(jsonPath("$.data.email").exists())
                .andExpect(jsonPath("$.data.password").exists());

        verifyNoInteractions(authService);
    }

    @Test
    void loginReturnsOkAndToken() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"dung@example.com","password":"password123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.accessToken").value("test-token"))
                .andExpect(jsonPath("$.data.expiresIn").value(3600));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void loginRejectsMissingCredentialsBeforeCallingService() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.data.email").exists())
                .andExpect(jsonPath("$.data.password").exists());

        verifyNoInteractions(authService);
    }

    @Test
    void resetPasswordAcceptsValidRequest() throws Exception {
        when(passwordResetService.resetPassword(any()))
                .thenReturn(new PasswordResetResultDto(null));
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"dung@example.com","recoveryCode":"test-recovery-code","newPassword":"newPassword123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset successfully"));

        verify(passwordResetService).resetPassword(any());
    }

    private AuthResponseDto authResponse() {
        UserDto user = new UserDto();
        user.setId(1L);
        user.setName("Dung");
        user.setEmail("dung@example.com");
        user.setRole(Role.USER);
        return new AuthResponseDto("test-token", "Bearer", 3600, user, "personal-recovery-code");
    }
}
