package com.dung.personal_finance_backend.security;

import com.dung.personal_finance_backend.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("userAccess")
public class UserAccess {
    public boolean canAccess(Long userId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        if (admin) {
            return true;
        }
        return authentication.getPrincipal() instanceof User user
                && user.getId().equals(userId);
    }
}
