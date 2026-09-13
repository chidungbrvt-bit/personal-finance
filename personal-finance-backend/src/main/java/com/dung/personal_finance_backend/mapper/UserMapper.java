package com.dung.personal_finance_backend.mapper;

import com.dung.personal_finance_backend.dto.UserDto;
import com.dung.personal_finance_backend.entity.User;

public class UserMapper {
    public static UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
