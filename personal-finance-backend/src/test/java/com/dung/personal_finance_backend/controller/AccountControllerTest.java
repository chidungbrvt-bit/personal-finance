package com.dung.personal_finance_backend.controller;

import com.dung.personal_finance_backend.dto.AccountDto;
import com.dung.personal_finance_backend.enums.AccountType;
import com.dung.personal_finance_backend.enums.CurrencyType;
import com.dung.personal_finance_backend.exception.AccountNotFoundException;
import com.dung.personal_finance_backend.exception.GlobalExceptionHandler;
import com.dung.personal_finance_backend.service.AccountService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {
    @Mock
    private AccountService accountService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AccountController(accountService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createAccountReturnsCreated() throws Exception {
        when(accountService.createAccount(eq(1L), any(AccountDto.class)))
                .thenReturn(account(10L));

        mockMvc.perform(post("/api/users/1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validAccountJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.currency").value("VND"));
    }

    @Test
    void createAccountRejectsInvalidBody() throws Exception {
        mockMvc.perform(post("/api/users/1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void getAccountsAndBalanceReturnData() throws Exception {
        when(accountService.getAccountsByUserId(1L)).thenReturn(List.of(account(10L)));
        when(accountService.getAccountBalance(1L, 10L)).thenReturn(new BigDecimal("1250000"));

        mockMvc.perform(get("/api/users/1/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));

        mockMvc.perform(get("/api/users/1/accounts/10/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1250000));
    }

    @Test
    void updateAndRestoreAccountReturnUpdatedAccount() throws Exception {
        when(accountService.updateAccount(eq(1L), eq(10L), any(AccountDto.class)))
                .thenReturn(account(10L));
        when(accountService.restoreAccount(1L, 10L)).thenReturn(account(10L));

        mockMvc.perform(put("/api/users/1/accounts/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validAccountJson()))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/users/1/accounts/10/restore"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(true));
    }

    @Test
    void deleteAccountCallsService() throws Exception {
        mockMvc.perform(delete("/api/users/1/accounts/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Account removed successfully"));

        verify(accountService).deleteAccount(1L, 10L);
    }

    @Test
    void getAccountReturnsNotFoundResponse() throws Exception {
        when(accountService.getAccountById(1L, 99L))
                .thenThrow(new AccountNotFoundException("Account not found with id: 99"));

        mockMvc.perform(get("/api/users/1/accounts/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Account not found with id: 99"));
    }

    private AccountDto account(Long id) {
        AccountDto dto = new AccountDto();
        dto.setId(id);
        dto.setName("Cash");
        dto.setType(AccountType.CASH);
        dto.setInitialBalance(new BigDecimal("1000000"));
        dto.setCurrency(CurrencyType.VND);
        dto.setActive(true);
        return dto;
    }

    private String validAccountJson() {
        return """
                {
                  "name":"Cash",
                  "type":"CASH",
                  "initialBalance":1000000,
                  "currency":"VND"
                }
                """;
    }
}
