package com.dung.personal_finance_backend.service;

import com.dung.personal_finance_backend.dto.AuthResponseDto;
import com.dung.personal_finance_backend.dto.UserDto;
import com.dung.personal_finance_backend.dto.request.LoginRequest;
import com.dung.personal_finance_backend.dto.request.RegisterRequest;
import com.dung.personal_finance_backend.entity.User;
import com.dung.personal_finance_backend.enums.Role;
import com.dung.personal_finance_backend.repository.UserRepository;
import com.dung.personal_finance_backend.security.JwtService;
import com.dung.personal_finance_backend.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    @Mock
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private RecoveryCodeService recoveryCodeService;

    private AuthServiceImpl authService;
    private User user;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                userService,
                userRepository,
                authenticationManager,
                jwtService,
                recoveryCodeService
        );
        user = new User();
        user.setId(1L);
        user.setName("Dung");
        user.setEmail("user@example.com");
        user.setPassword("encoded-password");
        user.setRole(Role.USER);
    }

    @Test
    void registerCreatesUserAndReturnsToken() {
        stubJwt();
        RegisterRequest request = new RegisterRequest(
                "Dung",
                "user@example.com",
                "password123"
        );
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        when(userService.createUser(request)).thenReturn(userDto);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(recoveryCodeService.generate()).thenReturn("personal-recovery-code");
        when(recoveryCodeService.hash("personal-recovery-code")).thenReturn("recovery-code-hash");

        AuthResponseDto response = authService.register(request);

        assertEquals("jwt-token", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(1L, response.getUser().getId());
        assertEquals("personal-recovery-code", response.getRecoveryCode());
        assertEquals("recovery-code-hash", user.getRecoveryCodeHash());
        verify(userService).createUser(request);
        verify(userRepository).save(user);
    }

    @Test
    void loginNormalizesEmailAndReturnsToken() {
        stubJwt();
        LoginRequest request = new LoginRequest("  USER@EXAMPLE.COM  ", "password123");
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                user,
                null,
                user.getAuthorities()
        );
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(authentication);

        AuthResponseDto response = authService.login(request);

        assertEquals("jwt-token", response.getAccessToken());
        assertEquals("user@example.com", response.getUser().getEmail());
        verify(authenticationManager).authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        "user@example.com",
                        "password123"
                )
        );
    }

    private void stubJwt() {
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);
    }
}
