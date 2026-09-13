package com.dung.personal_finance_backend.controller;

import com.dung.personal_finance_backend.dto.CategoryDto;
import com.dung.personal_finance_backend.response.ResponseHandler;
import com.dung.personal_finance_backend.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{userId}/categories")
@PreAuthorize("@userAccess.canAccess(#userId, authentication)")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<Object> createCategory(
            @PathVariable Long userId,
            @Valid @RequestBody CategoryDto categoryDto) {
        return ResponseHandler.responseBuilder(
                "Category created successfully",
                HttpStatus.CREATED,
                categoryService.createCategory(userId, categoryDto)
        );
    }

    @GetMapping
    public ResponseEntity<Object> getAllCategories(@PathVariable Long userId) {
        return ResponseHandler.responseBuilder(
                "Categories retrieved successfully",
                HttpStatus.OK,
                categoryService.getCategoriesByUserId(userId)
        );
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<Object> getCategoryById(
            @PathVariable Long userId,
            @PathVariable Long categoryId) {
        return ResponseHandler.responseBuilder(
                "Category retrieved successfully",
                HttpStatus.OK,
                categoryService.getCategoryById(userId, categoryId)
        );
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<Object> updateCategory(
            @PathVariable Long userId,
            @PathVariable Long categoryId,
            @Valid @RequestBody CategoryDto categoryDto) {
        return ResponseHandler.responseBuilder(
                "Category updated successfully",
                HttpStatus.OK,
                categoryService.updateCategory(userId, categoryId, categoryDto)
        );
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Object> deleteCategory(
            @PathVariable Long userId,
            @PathVariable Long categoryId) {
        categoryService.deleteCategory(userId, categoryId);
        return ResponseHandler.responseBuilder(
                "Category removed successfully",
                HttpStatus.OK,
                null
        );
    }
}
