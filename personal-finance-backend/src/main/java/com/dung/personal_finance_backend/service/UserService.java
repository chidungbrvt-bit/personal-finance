package com.dung.personal_finance_backend.service;

import com.dung.personal_finance_backend.dto.UserDto;
import com.dung.personal_finance_backend.dto.request.RegisterRequest;

import java.util.List;

public interface UserService {
    public UserDto createUser(RegisterRequest registerRequest);
    public UserDto getUserById(Long userId);
    public UserDto updateUser(Long userId, UserDto userDto);
    public List<UserDto> getAllUsers();
}
