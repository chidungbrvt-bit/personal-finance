package com.dung.personal_finance_backend.mapper;

import com.dung.personal_finance_backend.dto.CategoryDto;
import com.dung.personal_finance_backend.entity.Category;

public class CategoryMapper {

    public static CategoryDto toDto(Category category) {
        return new CategoryDto(
                category.getId(),
                category.getName(),
                category.getType(),
                category.getDescription(),
                category.getActive(),
                category.getCreatedAt()
        );
    }

    public static Category toEntity(CategoryDto categoryDto) {
        return new Category(
                null,
                categoryDto.getName(),
                categoryDto.getType(),
                categoryDto.getDescription(),
                categoryDto.getActive(),
                null,
                null,
                null
        );
    }
}
