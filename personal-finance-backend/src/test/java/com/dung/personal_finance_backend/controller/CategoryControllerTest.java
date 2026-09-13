package com.dung.personal_finance_backend.controller;

import com.dung.personal_finance_backend.dto.CategoryDto;
import com.dung.personal_finance_backend.enums.TransactionType;
import com.dung.personal_finance_backend.exception.CategoryNotFoundException;
import com.dung.personal_finance_backend.exception.GlobalExceptionHandler;
import com.dung.personal_finance_backend.service.CategoryService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {
    @Mock
    private CategoryService categoryService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new CategoryController(categoryService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createCategoryReturnsCreated() throws Exception {
        when(categoryService.createCategory(eq(1L), any(CategoryDto.class)))
                .thenReturn(category(5L));

        mockMvc.perform(post("/api/users/1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCategoryJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(5))
                .andExpect(jsonPath("$.data.type").value("EXPENSE"));
    }

    @Test
    void createCategoryRejectsInvalidBody() throws Exception {
        mockMvc.perform(post("/api/users/1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.name").exists())
                .andExpect(jsonPath("$.data.type").exists());
    }

    @Test
    void getCategoriesReturnsList() throws Exception {
        when(categoryService.getCategoriesByUserId(1L)).thenReturn(List.of(category(5L)));

        mockMvc.perform(get("/api/users/1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Food"));
    }

    @Test
    void updateAndDeleteCategoryCallService() throws Exception {
        when(categoryService.updateCategory(eq(1L), eq(5L), any(CategoryDto.class)))
                .thenReturn(category(5L));

        mockMvc.perform(put("/api/users/1/categories/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCategoryJson()))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/users/1/categories/5"))
                .andExpect(status().isOk());

        verify(categoryService).deleteCategory(1L, 5L);
    }

    @Test
    void getCategoryReturnsNotFoundResponse() throws Exception {
        when(categoryService.getCategoryById(1L, 99L))
                .thenThrow(new CategoryNotFoundException("Category not found with id: 99"));

        mockMvc.perform(get("/api/users/1/categories/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Category not found with id: 99"));
    }

    private CategoryDto category(Long id) {
        CategoryDto dto = new CategoryDto();
        dto.setId(id);
        dto.setName("Food");
        dto.setType(TransactionType.EXPENSE);
        dto.setActive(true);
        return dto;
    }

    private String validCategoryJson() {
        return """
                {"name":"Food","type":"EXPENSE","description":"Daily meals"}
                """;
    }
}
