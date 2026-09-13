package com.dung.personal_finance_backend.security;

import com.dung.personal_finance_backend.entity.User;
import com.dung.personal_finance_backend.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserAccessTest {
    private final UserAccess userAccess = new UserAccess();

    @Test
    void userCanAccessOwnResources() {
        Authentication authentication = authenticatedUser(1L, Role.USER);

        assertTrue(userAccess.canAccess(1L, authentication));
    }

    @Test
    void userCannotAccessAnotherUsersResources() {
        Authentication authentication = authenticatedUser(1L, Role.USER);

        assertFalse(userAccess.canAccess(2L, authentication));
    }

    @Test
    void adminCanAccessAnotherUsersResources() {
        Authentication authentication = authenticatedUser(1L, Role.ADMIN);

        assertTrue(userAccess.canAccess(2L, authentication));
    }

    private Authentication authenticatedUser(Long id, Role role) {
        User user = new User();
        user.setId(id);
        user.setEmail("user@example.com");
        user.setPassword("encoded-password");
        user.setRole(role);
        return UsernamePasswordAuthenticationToken.authenticated(
                user,
                null,
                user.getAuthorities()
        );
    }
}
