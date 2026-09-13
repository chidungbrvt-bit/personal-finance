package com.dung.personal_finance_backend.controller;

import com.dung.personal_finance_backend.dto.UserDto;
import com.dung.personal_finance_backend.response.ResponseHandler;
import com.dung.personal_finance_backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}")
    @PreAuthorize("@userAccess.canAccess(#userId, authentication)")
    public ResponseEntity<Object> getUserById(@PathVariable Long userId) {
        return ResponseHandler.responseBuilder(
                "User retrieved successfully",
                HttpStatus.OK,
                userService.getUserById(userId));
    }

    @PutMapping("/{userId}")
    @PreAuthorize("@userAccess.canAccess(#userId, authentication)")
    public ResponseEntity<Object> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserDto userDto
    ) {
        return ResponseHandler.responseBuilder(
                "User updated successfully",
                HttpStatus.OK,
                userService.updateUser(userId, userDto)
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getAllUsers() {
        return ResponseHandler.responseBuilder(
                "Users retrieved successfully",
                HttpStatus.OK,
                userService.getAllUsers()
        );
    }
}
