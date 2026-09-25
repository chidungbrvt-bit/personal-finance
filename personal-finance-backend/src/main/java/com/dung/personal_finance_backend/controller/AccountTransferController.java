package com.dung.personal_finance_backend.controller;

import com.dung.personal_finance_backend.dto.AccountTransferDto;
import com.dung.personal_finance_backend.response.ResponseHandler;
import com.dung.personal_finance_backend.service.AccountTransferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/{userId}/transfers")
@PreAuthorize("@userAccess.canAccess(#userId, authentication)")
public class AccountTransferController {
    private final AccountTransferService transferService;

    public AccountTransferController(AccountTransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public ResponseEntity<Object> createTransfer(
            @PathVariable Long userId,
            @Valid @RequestBody AccountTransferDto request
    ) {
        return ResponseHandler.responseBuilder(
                "Money transferred successfully",
                HttpStatus.CREATED,
                transferService.createTransfer(userId, request)
        );
    }

    @GetMapping
    public ResponseEntity<Object> getTransfers(@PathVariable Long userId) {
        return ResponseHandler.responseBuilder(
                "Account transfers retrieved successfully",
                HttpStatus.OK,
                transferService.getTransfers(userId)
        );
    }
}
