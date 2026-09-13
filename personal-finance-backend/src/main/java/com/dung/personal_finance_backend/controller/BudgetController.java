package com.dung.personal_finance_backend.controller;

import com.dung.personal_finance_backend.dto.BudgetDto;
import com.dung.personal_finance_backend.exception.BusinessRuleException;
import com.dung.personal_finance_backend.response.ResponseHandler;
import com.dung.personal_finance_backend.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{userId}/budgets")
@PreAuthorize("@userAccess.canAccess(#userId, authentication)")
public class BudgetController {
    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    public ResponseEntity<Object> createBudget(
            @PathVariable Long userId,
            @Valid @RequestBody BudgetDto budgetDto
    ) {
        return ResponseHandler.responseBuilder(
                "Budget created successfully",
                HttpStatus.CREATED,
                budgetService.createBudget(userId, budgetDto)
        );
    }

    @GetMapping
    public ResponseEntity<Object> getBudgets(
            @PathVariable Long userId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        if ((year == null) != (month == null)) {
            throw new BusinessRuleException(
                    "Both year and month must be provided together"
            );
        }

        Object data = year == null
                ? budgetService.getBudgetsByUserId(userId)
                : budgetService.getBudgetsByMonth(userId, year, month);

        return ResponseHandler.responseBuilder(
                "Budgets retrieved successfully",
                HttpStatus.OK,
                data
        );
    }

    @GetMapping("/{budgetId}")
    public ResponseEntity<Object> getBudgetById(
            @PathVariable Long userId,
            @PathVariable Long budgetId
    ) {
        return ResponseHandler.responseBuilder(
                "Budget retrieved successfully",
                HttpStatus.OK,
                budgetService.getBudgetById(userId, budgetId)
        );
    }

    @GetMapping("/{budgetId}/progress")
    public ResponseEntity<Object> getBudgetProgress (
            @PathVariable Long userId,
            @PathVariable Long budgetId
    ) {
        return ResponseHandler.responseBuilder(
                "Budget progress retrieved successfully",
                HttpStatus.OK,
                budgetService.getBudgetProgress(userId, budgetId)
        );
    }

    @PutMapping("/{budgetId}")
    public ResponseEntity<Object> updateBudget(
            @PathVariable Long userId,
            @PathVariable Long budgetId,
            @Valid @RequestBody BudgetDto budgetDto
    ) {
        return ResponseHandler.responseBuilder(
                "Budget updated successfully",
                HttpStatus.OK,
                budgetService.updateBudget(userId, budgetId, budgetDto)
        );
    }

    @DeleteMapping("/{budgetId}")
    public ResponseEntity<Object> deleteBudget(
            @PathVariable Long userId,
            @PathVariable Long budgetId
    ) {
        budgetService.deleteBudget(userId, budgetId);
        return ResponseHandler.responseBuilder(
                "Budget deleted successfully",
                HttpStatus.OK,
                null
        );
    }
}
