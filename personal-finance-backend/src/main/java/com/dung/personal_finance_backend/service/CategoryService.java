package com.dung.personal_finance_backend.service;

import com.dung.personal_finance_backend.dto.CategoryDto;

import java.util.List;

public interface CategoryService {
    public CategoryDto createCategory(Long userId, CategoryDto categoryDto);
    public List<CategoryDto> getCategoriesByUserId(Long userId);
    public CategoryDto getCategoryById(Long userId, Long categoryId);
    public CategoryDto updateCategory(
            Long userId,
            Long categoryId,
            CategoryDto categoryDto
    );
    public void deleteCategory(Long userId, Long categoryId);
}
