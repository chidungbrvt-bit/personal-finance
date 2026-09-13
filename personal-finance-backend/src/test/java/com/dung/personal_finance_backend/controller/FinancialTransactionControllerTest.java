package com.dung.personal_finance_backend.controller;

import com.dung.personal_finance_backend.dto.FinancialTransactionDto;
import com.dung.personal_finance_backend.enums.TransactionType;
import com.dung.personal_finance_backend.exception.FinancialTransactionNotFoundException;
import com.dung.personal_finance_backend.exception.GlobalExceptionHandler;
import com.dung.personal_finance_backend.service.FinancialTransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
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
class FinancialTransactionControllerTest {
    @Mock
    private FinancialTransactionService transactionService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new FinancialTransactionController(transactionService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createTransactionReturnsCreated() throws Exception {
        when(transactionService.createFinancialTransaction(
                eq(1L), any(FinancialTransactionDto.class)))
                .thenReturn(transaction(7L));

        mockMvc.perform(post("/api/users/1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validTransactionJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(7))
                .andExpect(jsonPath("$.data.type").value("EXPENSE"));
    }

    @Test
    void createTransactionRejectsInvalidBody() throws Exception {
        mockMvc.perform(post("/api/users/1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void getTransactionsFiltersByType() throws Exception {
        when(transactionService.getFinancialTransactionsByType(1L, TransactionType.EXPENSE))
                .thenReturn(List.of(transaction(7L)));

        mockMvc.perform(get("/api/users/1/transactions")
                        .param("type", "EXPENSE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(transactionService)
                .getFinancialTransactionsByType(1L, TransactionType.EXPENSE);
    }

    @Test
    void getTransactionsCalculatesMonthlyDateRange() throws Exception {
        when(transactionService.getFinancialTransactionsByDateRange(
                1L,
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 10, 1)))
                .thenReturn(List.of(transaction(7L)));

        mockMvc.perform(get("/api/users/1/transactions")
                        .param("period", "MONTH")
                        .param("year", "2026")
                        .param("month", "9"))
                .andExpect(status().isOk());

        verify(transactionService).getFinancialTransactionsByDateRange(
                1L,
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 10, 1)
        );
    }

    @Test
    void getTransactionsRejectsMultipleMainFilters() throws Exception {
        mockMvc.perform(get("/api/users/1/transactions")
                        .param("accountId", "2")
                        .param("categoryId", "3"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Use only one of accountId, categoryId, or type"));

        verifyNoInteractions(transactionService);
    }

    @Test
    void updateAndDeleteTransactionCallService() throws Exception {
        when(transactionService.updateFinancialTransaction(
                eq(1L), eq(7L), any(FinancialTransactionDto.class)))
                .thenReturn(transaction(7L));

        mockMvc.perform(put("/api/users/1/transactions/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validTransactionJson()))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/users/1/transactions/7"))
                .andExpect(status().isOk());

        verify(transactionService).deleteFinancialTransaction(1L, 7L);
    }

    @Test
    void getTransactionReturnsNotFoundResponse() throws Exception {
        when(transactionService.getFinancialTransactionById(1L, 99L))
                .thenThrow(new FinancialTransactionNotFoundException(
                        "Financial transaction not found with id: 99"));

        mockMvc.perform(get("/api/users/1/transactions/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Financial transaction not found with id: 99"));
    }

    private FinancialTransactionDto transaction(Long id) {
        FinancialTransactionDto dto = new FinancialTransactionDto();
        dto.setId(id);
        dto.setType(TransactionType.EXPENSE);
        dto.setAmount(new BigDecimal("150000"));
        dto.setTransactionDate(LocalDate.of(2026, 9, 12));
        dto.setAccountId(2L);
        dto.setCategoryId(3L);
        return dto;
    }

    private String validTransactionJson() {
        return """
                {
                  "type":"EXPENSE",
                  "amount":150000,
                  "transactionDate":"2026-09-12",
                  "description":"Lunch",
                  "accountId":2,
                  "categoryId":3
                }
                """;
    }
}
