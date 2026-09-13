package com.dung.personal_finance_backend.service.impl;

import com.dung.personal_finance_backend.dto.AuthResponseDto;
import com.dung.personal_finance_backend.dto.UserDto;
import com.dung.personal_finance_backend.dto.request.LoginRequest;
import com.dung.personal_finance_backend.dto.request.RegisterRequest;
import com.dung.personal_finance_backend.entity.User;
import com.dung.personal_finance_backend.exception.BusinessRuleException;
import com.dung.personal_finance_backend.repository.UserRepository;
import com.dung.personal_finance_backend.security.JwtService;
import com.dung.personal_finance_backend.service.AuthService;
import com.dung.personal_finance_backend.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserService userService;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthServiceImpl(
            UserService userService,
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    public AuthResponseDto register(RegisterRequest request) {
        if (userRepository.count() > 0) {
            throw new BusinessRuleException("Registration is closed for this single-user application");
        }
        UserDto createdUser = userService.createUser(request);
        User user = userRepository.findById(createdUser.getId())
                .orElseThrow(() -> new IllegalStateException("Created user could not be loaded"));

        return buildResponse(user);
    }

    @Override
    public AuthResponseDto login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase(Locale.ROOT);
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        normalizedEmail,
                        request.getPassword()
                )
        );
        User user = (User) authentication.getPrincipal();
        return buildResponse(user);
    }

    private AuthResponseDto buildResponse(User user) {
        return new AuthResponseDto(
                jwtService.generateToken(user),
                "Bearer",
                jwtService.getExpirationSeconds(),
                com.dung.personal_finance_backend.mapper.UserMapper.toDto(user)
        );
    }
}
