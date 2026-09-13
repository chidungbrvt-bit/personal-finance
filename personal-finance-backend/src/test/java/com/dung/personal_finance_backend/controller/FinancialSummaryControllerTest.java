package com.dung.personal_finance_backend.controller;

import com.dung.personal_finance_backend.dto.FinancialSummaryDto;
import com.dung.personal_finance_backend.dto.FinancialSummaryRequestDto;
import com.dung.personal_finance_backend.enums.PeriodType;
import com.dung.personal_finance_backend.exception.BusinessRuleException;
import com.dung.personal_finance_backend.exception.GlobalExceptionHandler;
import com.dung.personal_finance_backend.service.FinancialSummaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FinancialSummaryControllerTest {
    @Mock
    private FinancialSummaryService financialSummaryService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new FinancialSummaryController(financialSummaryService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getMonthlySummaryBindsQueryParametersAndReturnsData() throws Exception {
        when(financialSummaryService.getSummary(eq(1L), any(FinancialSummaryRequestDto.class)))
                .thenReturn(monthlySummary());

        mockMvc.perform(get("/api/users/1/summary")
                        .param("period", "MONTH")
                        .param("year", "2026")
                        .param("month", "9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Financial summary retrieved successfully"))
                .andExpect(jsonPath("$.data.period").value("MONTH"))
                .andExpect(jsonPath("$.data.totalIncome").value(3000000))
                .andExpect(jsonPath("$.data.totalExpenses").value(1200000))
                .andExpect(jsonPath("$.data.netAmount").value(1800000))
                .andExpect(jsonPath("$.data.expensesByCategory.Food").value(1000000));

        ArgumentCaptor<FinancialSummaryRequestDto> requestCaptor =
                ArgumentCaptor.forClass(FinancialSummaryRequestDto.class);
        verify(financialSummaryService).getSummary(eq(1L), requestCaptor.capture());
        assertEquals(PeriodType.MONTH, requestCaptor.getValue().getPeriod());
        assertEquals(2026, requestCaptor.getValue().getYear());
        assertEquals(9, requestCaptor.getValue().getMonth());
    }

    @Test
    void getCustomSummaryBindsIsoDates() throws Exception {
        when(financialSummaryService.getSummary(eq(1L), any(FinancialSummaryRequestDto.class)))
                .thenReturn(monthlySummary());

        mockMvc.perform(get("/api/users/1/summary")
                        .param("period", "CUSTOM")
                        .param("fromDate", "2026-09-01")
                        .param("toDate", "2026-09-30"))
                .andExpect(status().isOk());

        ArgumentCaptor<FinancialSummaryRequestDto> requestCaptor =
                ArgumentCaptor.forClass(FinancialSummaryRequestDto.class);
        verify(financialSummaryService).getSummary(eq(1L), requestCaptor.capture());
        assertEquals(LocalDate.of(2026, 9, 1), requestCaptor.getValue().getFromDate());
        assertEquals(LocalDate.of(2026, 9, 30), requestCaptor.getValue().getToDate());
    }

    @Test
    void getSummaryRejectsInvalidPeriodBeforeCallingService() throws Exception {
        mockMvc.perform(get("/api/users/1/summary")
                .param("period", "WEEK"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verifyNoInteractions(financialSummaryService);
    }

    @Test
    void getSummaryReturnsBusinessRuleErrorFromService() throws Exception {
        when(financialSummaryService.getSummary(eq(1L), any(FinancialSummaryRequestDto.class)))
                .thenThrow(new BusinessRuleException("Period is required"));

        mockMvc.perform(get("/api/users/1/summary"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Period is required"));
    }

    private FinancialSummaryDto monthlySummary() {
        return new FinancialSummaryDto(
                PeriodType.MONTH,
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30),
                new BigDecimal("3000000"),
                new BigDecimal("1200000"),
                new BigDecimal("1800000"),
                Map.of("Salary", new BigDecimal("3000000")),
                Map.of(
                        "Food", new BigDecimal("1000000"),
                        "Transport", new BigDecimal("200000")
                )
        );
    }
}
