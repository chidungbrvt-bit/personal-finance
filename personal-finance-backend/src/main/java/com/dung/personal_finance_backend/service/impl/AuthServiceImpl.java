package com.dung.personal_finance_backend.service.impl;

import com.dung.personal_finance_backend.dto.AuthResponseDto;
import com.dung.personal_finance_backend.dto.UserDto;
import com.dung.personal_finance_backend.dto.request.LoginRequest;
import com.dung.personal_finance_backend.dto.request.RegisterRequest;
import com.dung.personal_finance_backend.entity.User;
import com.dung.personal_finance_backend.repository.UserRepository;
import com.dung.personal_finance_backend.security.JwtService;
import com.dung.personal_finance_backend.service.AuthService;
import com.dung.personal_finance_backend.service.RecoveryCodeService;
import com.dung.personal_finance_backend.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserService userService;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RecoveryCodeService recoveryCodeService;

    public AuthServiceImpl(
            UserService userService,
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            RecoveryCodeService recoveryCodeService
    ) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.recoveryCodeService = recoveryCodeService;
    }

    @Override
    @Transactional
    public AuthResponseDto register(RegisterRequest request) {
        UserDto createdUser = userService.createUser(request);
        User user = userRepository.findById(createdUser.getId())
                .orElseThrow(() -> new IllegalStateException("Created user could not be loaded"));

        String recoveryCode = recoveryCodeService.generate();
        user.setRecoveryCodeHash(recoveryCodeService.hash(recoveryCode));
        userRepository.save(user);

        return buildResponse(user, recoveryCode);
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
        return buildResponse(user, null);
    }

    private AuthResponseDto buildResponse(User user, String recoveryCode) {
        return new AuthResponseDto(
                jwtService.generateToken(user),
                "Bearer",
                jwtService.getExpirationSeconds(),
                com.dung.personal_finance_backend.mapper.UserMapper.toDto(user),
                recoveryCode
        );
    }
}
