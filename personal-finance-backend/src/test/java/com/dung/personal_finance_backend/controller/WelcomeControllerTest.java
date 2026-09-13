package com.dung.personal_finance_backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WelcomeControllerTest {
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new WelcomeController()).build();
    }

    @Test
    void welcomeReturnsPublicApiStatus() throws Exception {
        mockMvc.perform(get("/api/public/welcome"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Welcome to Personal Finance API"))
                .andExpect(jsonPath("$.data").value("Personal Finance Backend is running"))
                .andExpect(jsonPath("$.httpStatus").value("200 OK"));
    }
}
