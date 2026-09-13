package com.dung.personal_finance_backend.controller;

import com.dung.personal_finance_backend.dto.AccountDto;
import com.dung.personal_finance_backend.response.ResponseHandler;
import com.dung.personal_finance_backend.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/{userId}/accounts")
@PreAuthorize("@userAccess.canAccess(#userId, authentication)")
public class AccountController {
    AccountService accountService;
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }
    @PostMapping
    public ResponseEntity<Object> createAccount(
            @PathVariable Long userId,
            @Valid @RequestBody AccountDto accountDto) {
        return ResponseHandler.responseBuilder("Account created successfully",
                HttpStatus.CREATED, accountService.createAccount(userId, accountDto));
    }

    @GetMapping
    public ResponseEntity<Object> getAllAccounts(@PathVariable Long userId) {
        return ResponseHandler.responseBuilder("Account retrieved successfully",
                HttpStatus.OK, accountService.getAccountsByUserId(userId));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<Object> getAccountById(
            @PathVariable Long userId,
            @PathVariable Long accountId) {
        return ResponseHandler.responseBuilder("Account retrieved successfully",
                HttpStatus.OK, accountService.getAccountById(userId, accountId));
    }

    @GetMapping("/{accountId}/balance")
    public ResponseEntity<Object> getAccountBalance(
            @PathVariable Long userId,
            @PathVariable Long accountId
    ) {
        return ResponseHandler.responseBuilder(
                "Account balance retrieved successfully",
                HttpStatus.OK,
                accountService.getAccountBalance(userId, accountId)
        );
    }

    @GetMapping("/archived")
    public ResponseEntity<Object> getArchivedAccounts(@PathVariable Long userId) {
        return ResponseHandler.responseBuilder(
                "Archived accounts retrieved successfully",
                HttpStatus.OK,
                accountService.getArchivedAccounts(userId)
        );
    }

    @PutMapping("/{accountId}")
    public ResponseEntity<Object> updateAccount(
            @PathVariable Long userId,
            @PathVariable Long accountId,
            @Valid @RequestBody AccountDto accountDto) {
        return ResponseHandler.responseBuilder("Account updated successfully",
                HttpStatus.OK, accountService.updateAccount(userId, accountId, accountDto));
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<Object> deleteAccount(
            @PathVariable Long userId,
            @PathVariable Long accountId) {
        accountService.deleteAccount(userId, accountId);
        return ResponseHandler.responseBuilder("Account removed successfully",
                HttpStatus.OK, null);
    }

    @PatchMapping("/{accountId}/restore")
    public ResponseEntity<Object> restoreAccount(
            @PathVariable Long userId,
            @PathVariable Long accountId
    ) {
        return ResponseHandler.responseBuilder(
                "Account restored successfully",
                HttpStatus.OK,
                accountService.restoreAccount(userId, accountId)
        );
    }
}
