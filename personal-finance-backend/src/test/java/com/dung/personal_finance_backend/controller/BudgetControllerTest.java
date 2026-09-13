package com.dung.personal_finance_backend.controller;

import com.dung.personal_finance_backend.dto.BudgetDto;
import com.dung.personal_finance_backend.dto.BudgetProgressDto;
import com.dung.personal_finance_backend.exception.BudgetNotFoundException;
import com.dung.personal_finance_backend.exception.GlobalExceptionHandler;
import com.dung.personal_finance_backend.service.BudgetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BudgetControllerTest {
    @Mock
    private BudgetService budgetService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new BudgetController(budgetService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createBudgetReturnsCreated() throws Exception {
        when(budgetService.createBudget(eq(1L), any(BudgetDto.class)))
                .thenReturn(budget(8L));

        mockMvc.perform(post("/api/users/1/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBudgetJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(8))
                .andExpect(jsonPath("$.data.amount").value(1000000));
    }

    @Test
    void createBudgetRejectsInvalidBody() throws Exception {
        mockMvc.perform(post("/api/users/1/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"year\":2025,\"month\":13,\"amount\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void getBudgetsWithoutFilterReturnsAll() throws Exception {
        when(budgetService.getBudgetsByUserId(1L)).thenReturn(List.of(budget(8L)));

        mockMvc.perform(get("/api/users/1/budgets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void getBudgetsWithYearAndMonthUsesMonthlyService() throws Exception {
        when(budgetService.getBudgetsByMonth(1L, 2026, 9))
                .thenReturn(List.of(budget(8L)));

        mockMvc.perform(get("/api/users/1/budgets")
                        .param("year", "2026")
                        .param("month", "9"))
                .andExpect(status().isOk());

        verify(budgetService).getBudgetsByMonth(1L, 2026, 9);
    }

    @Test
    void getBudgetsRejectsIncompleteDateFilter() throws Exception {
        mockMvc.perform(get("/api/users/1/budgets")
                        .param("year", "2026"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Both year and month must be provided together"));

        verifyNoInteractions(budgetService);
    }

    @Test
    void getBudgetProgressReturnsCalculatedFields() throws Exception {
        BudgetProgressDto progress = new BudgetProgressDto(
                8L, 3L, "Food", 2026, 9,
                new BigDecimal("1000000"),
                new BigDecimal("600000"),
                new BigDecimal("400000"),
                new BigDecimal("60.00"),
                false
        );
        when(budgetService.getBudgetProgress(1L, 8L)).thenReturn(progress);

        mockMvc.perform(get("/api/users/1/budgets/8/progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.remainingAmount").value(400000))
                .andExpect(jsonPath("$.data.overBudget").value(false));
    }

    @Test
    void updateAndDeleteBudgetCallService() throws Exception {
        when(budgetService.updateBudget(eq(1L), eq(8L), any(BudgetDto.class)))
                .thenReturn(budget(8L));

        mockMvc.perform(put("/api/users/1/budgets/8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBudgetJson()))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/users/1/budgets/8"))
                .andExpect(status().isOk());

        verify(budgetService).deleteBudget(1L, 8L);
    }

    @Test
    void getBudgetReturnsNotFoundResponse() throws Exception {
        when(budgetService.getBudgetById(1L, 99L))
                .thenThrow(new BudgetNotFoundException("Budget not found with id: 99"));

        mockMvc.perform(get("/api/users/1/budgets/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Budget not found with id: 99"));
    }

    private BudgetDto budget(Long id) {
        BudgetDto dto = new BudgetDto();
        dto.setId(id);
        dto.setYear(2026);
        dto.setMonth(9);
        dto.setAmount(new BigDecimal("1000000"));
        dto.setCategoryId(3L);
        return dto;
    }

    private String validBudgetJson() {
        return """
                {"year":2026,"month":9,"amount":1000000,"categoryId":3}
                """;
    }
}
