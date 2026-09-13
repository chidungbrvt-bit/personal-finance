package com.dung.personal_finance_backend.controller;


import com.dung.personal_finance_backend.dto.FinancialSummaryRequestDto;
import com.dung.personal_finance_backend.response.ResponseHandler;
import com.dung.personal_finance_backend.service.FinancialSummaryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/{userId}/summary")
@PreAuthorize("@userAccess.canAccess(#userId, authentication)")
public class FinancialSummaryController {
    private final FinancialSummaryService financialSummaryService;
    public FinancialSummaryController(FinancialSummaryService financialSummaryService) {
        this.financialSummaryService = financialSummaryService;
    }

    @GetMapping()
    public ResponseEntity<Object> getSummary (
            @PathVariable Long userId,
            @ModelAttribute FinancialSummaryRequestDto request
    ) {
        return ResponseHandler.responseBuilder(
                "Financial summary retrieved successfully",
                HttpStatus.OK,
                financialSummaryService.getSummary(
                        userId,
                        request
                )
        );
    }
}
