package com.dung.personal_finance_backend.service;

import com.dung.personal_finance_backend.dto.CategoryDto;
import com.dung.personal_finance_backend.entity.User;
import com.dung.personal_finance_backend.exception.CategoryNotFoundException;
import com.dung.personal_finance_backend.exception.UserNotFoundException;
import com.dung.personal_finance_backend.repository.BudgetRepository;
import com.dung.personal_finance_backend.repository.CategoryRepository;
import com.dung.personal_finance_backend.repository.FinancialTransactionRepository;
import com.dung.personal_finance_backend.repository.UserRepository;
import com.dung.personal_finance_backend.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {
    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FinancialTransactionRepository financialTransactionRepository;

    @Mock
    private BudgetRepository budgetRepository;

    private CategoryServiceImpl categoryService;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryServiceImpl(
                categoryRepository,
                userRepository,
                financialTransactionRepository,
                budgetRepository
        );
    }

    @Test
    void createCategory_shouldThrowWhenUserDoesNotExist() {
        Long userId = 999L;
        CategoryDto categoryDto = new CategoryDto();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> categoryService.createCategory(userId, categoryDto)
        );

        assertEquals("User not found with id: 999", exception.getMessage());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void getCategoryById_shouldThrowWhenCategoryDoesNotExist() {
        Long userId = 1L;
        Long categoryId = 999L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(categoryRepository.findByIdAndUserId(categoryId, userId))
                .thenReturn(Optional.empty());

        CategoryNotFoundException exception = assertThrows(
                CategoryNotFoundException.class,
                () -> categoryService.getCategoryById(userId, categoryId)
        );

        assertEquals("Category not found with id: 999", exception.getMessage());
    }
}
