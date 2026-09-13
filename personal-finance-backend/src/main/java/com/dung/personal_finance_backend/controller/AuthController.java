package com.dung.personal_finance_backend.controller;

import com.dung.personal_finance_backend.dto.request.LoginRequest;
import com.dung.personal_finance_backend.dto.request.RegisterRequest;
import com.dung.personal_finance_backend.dto.request.ForgotPasswordRequest;
import com.dung.personal_finance_backend.dto.request.ResetPasswordRequest;
import com.dung.personal_finance_backend.response.ResponseHandler;
import com.dung.personal_finance_backend.service.AuthService;
import com.dung.personal_finance_backend.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthService authService, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/register")
    public ResponseEntity<Object> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseHandler.responseBuilder(
                "User registered successfully",
                HttpStatus.CREATED,
                authService.register(request)
        );
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseHandler.responseBuilder(
                "Login successful",
                HttpStatus.OK,
                authService.login(request)
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Object> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        passwordResetService.requestReset(request.getEmail());
        return ResponseHandler.responseBuilder(
                "If an account exists for that email, a password reset link has been sent",
                HttpStatus.OK,
                null
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Object> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        passwordResetService.resetPassword(request);
        return ResponseHandler.responseBuilder(
                "Password reset successfully",
                HttpStatus.OK,
                null
        );
    }
}
