package com.dung.personal_finance_backend.controller;

import com.dung.personal_finance_backend.dto.UserDto;
import com.dung.personal_finance_backend.enums.Role;
import com.dung.personal_finance_backend.exception.GlobalExceptionHandler;
import com.dung.personal_finance_backend.exception.UserNotFoundException;
import com.dung.personal_finance_backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    @Mock
    private UserService userService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new UserController(userService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getUserByIdReturnsUser() throws Exception {
        when(userService.getUserById(1L)).thenReturn(user(1L));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.role").value("USER"));
    }

    @Test
    void getAllUsersReturnsList() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(user(1L), user(2L)));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void updateUserReturnsUpdatedUser() throws Exception {
        when(userService.updateUser(eq(1L), any(UserDto.class))).thenReturn(user(1L));

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Dung","email":"dung@example.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("user1@example.com"));

        verify(userService).updateUser(eq(1L), any(UserDto.class));
    }

    @Test
    void updateUserRejectsInvalidBody() throws Exception {
        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","email":"not-an-email"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.data.name").exists())
                .andExpect(jsonPath("$.data.email").exists());
    }

    @Test
    void getUserReturnsNotFoundResponse() throws Exception {
        when(userService.getUserById(99L))
                .thenThrow(new UserNotFoundException("User not found with id: 99"));

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id: 99"));
    }

    private UserDto user(Long id) {
        UserDto dto = new UserDto();
        dto.setId(id);
        dto.setName("User " + id);
        dto.setEmail("user" + id + "@example.com");
        dto.setRole(Role.USER);
        return dto;
    }
}
