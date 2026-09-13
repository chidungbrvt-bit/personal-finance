package com.dung.personal_finance_backend.security;

import com.dung.personal_finance_backend.entity.User;
import com.dung.personal_finance_backend.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {
    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(
                "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
                3_600_000
        );
        user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setPassword("encoded-password");
        user.setRole(Role.USER);
    }

    @Test
    void generateTokenContainsUserEmail() {
        String token = jwtService.generateToken(user);

        assertEquals("user@example.com", jwtService.extractUsername(token));
    }

    @Test
    void generatedTokenIsValidForSameUser() {
        String token = jwtService.generateToken(user);

        assertTrue(jwtService.isTokenValid(token, user));
        assertEquals(3600, jwtService.getExpirationSeconds());
    }
}
