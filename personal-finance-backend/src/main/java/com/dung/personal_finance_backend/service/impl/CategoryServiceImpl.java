package com.dung.personal_finance_backend.service.impl;

import com.dung.personal_finance_backend.dto.CategoryDto;
import com.dung.personal_finance_backend.entity.Category;
import com.dung.personal_finance_backend.entity.User;
import com.dung.personal_finance_backend.exception.CategoryNotFoundException;
import com.dung.personal_finance_backend.exception.UserNotFoundException;
import com.dung.personal_finance_backend.mapper.CategoryMapper;
import com.dung.personal_finance_backend.repository.BudgetRepository;
import com.dung.personal_finance_backend.repository.CategoryRepository;
import com.dung.personal_finance_backend.repository.FinancialTransactionRepository;
import com.dung.personal_finance_backend.repository.UserRepository;
import com.dung.personal_finance_backend.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final FinancialTransactionRepository financialTransactionRepository;
    private final BudgetRepository budgetRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository,
                               UserRepository userRepository,
                               FinancialTransactionRepository financialTransactionRepository,
                               BudgetRepository budgetRepository
    ) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.financialTransactionRepository = financialTransactionRepository;
        this.budgetRepository = budgetRepository;
    }

    @Override
    public CategoryDto createCategory(Long userId, CategoryDto categoryDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        Category category = CategoryMapper.toEntity(categoryDto);
        if (category.getActive() == null) {
            category.setActive(true);
        }
        category.setUser(user);
        Category savedCategory = categoryRepository.save(category);
        return CategoryMapper.toDto(savedCategory);
    }

    @Override
    public List<CategoryDto> getCategoriesByUserId(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        List<Category> categories = categoryRepository.findByUserIdAndActiveTrue(userId);
        List<CategoryDto> categoryDtos = new ArrayList<>();
        for (Category category : categories) {
            categoryDtos.add(CategoryMapper.toDto(category));
        }
        return categoryDtos;
    }

    @Override
    public CategoryDto getCategoryById(Long userId, Long categoryId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new CategoryNotFoundException(
                        "Category not found with id: " + categoryId));
        return CategoryMapper.toDto(category);
    }

    @Override
    public CategoryDto updateCategory(
            Long userId,
            Long categoryId,
            CategoryDto categoryDto
    ) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new CategoryNotFoundException(
                        "Category not found with id: " + categoryId));

        category.setName(categoryDto.getName());
        category.setType(categoryDto.getType());
        category.setDescription(categoryDto.getDescription());
        if (categoryDto.getActive() != null) {
            category.setActive(categoryDto.getActive());
        }

        Category updatedCategory = categoryRepository.save(category);
        return CategoryMapper.toDto(updatedCategory);
    }

    @Override
    public void deleteCategory(Long userId, Long categoryId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new CategoryNotFoundException(
                        "Category not found with id: " + categoryId));
        Boolean usedByTransactions = financialTransactionRepository.existsByCategoryIdAndUserId(categoryId, userId);
        Boolean usedByBudgets = budgetRepository.existsByCategoryIdAndUserId(categoryId, userId);
        if (usedByBudgets || usedByTransactions) {
            category.setActive(false);
            categoryRepository.save(category);
        } else {
            categoryRepository.delete(category);
        }
    }
}
