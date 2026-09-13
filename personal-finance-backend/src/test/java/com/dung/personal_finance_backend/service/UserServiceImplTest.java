package com.dung.personal_finance_backend.service;

import com.dung.personal_finance_backend.dto.UserDto;
import com.dung.personal_finance_backend.dto.request.RegisterRequest;
import com.dung.personal_finance_backend.entity.User;
import com.dung.personal_finance_backend.enums.Role;
import com.dung.personal_finance_backend.exception.BusinessRuleException;
import com.dung.personal_finance_backend.exception.UserNotFoundException;
import com.dung.personal_finance_backend.repository.UserRepository;
import com.dung.personal_finance_backend.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, passwordEncoder);
    }

    @Test
    void createUser_shouldCreateUserSuccessfully() {
        RegisterRequest registerRequest = createRegisterRequest(
                "  Alice  ",
                "alice@example.com",
                "password123"
        );

        when(userRepository.existsByEmailIgnoreCase("alice@example.com"))
                .thenReturn(false);
        when(passwordEncoder.encode("password123"))
                .thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        UserDto result = userService.createUser(registerRequest);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User userToSave = userCaptor.getValue();

        assertEquals(1L, result.getId());
        assertEquals("Alice", result.getName());
        assertEquals("alice@example.com", result.getEmail());
        assertEquals(Role.USER, result.getRole());
        assertEquals("Alice", userToSave.getName());
        assertEquals("alice@example.com", userToSave.getEmail());
        assertEquals("encoded-password", userToSave.getPassword());
        assertEquals(Role.USER, userToSave.getRole());
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void createUser_shouldTrimAndLowercaseEmail() {
        RegisterRequest registerRequest = createRegisterRequest(
                "Alice",
                "  Alice@Example.COM  ",
                "password123"
        );

        when(userRepository.existsByEmailIgnoreCase("alice@example.com"))
                .thenReturn(false);
        when(passwordEncoder.encode("password123"))
                .thenReturn("encoded-password");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = userService.createUser(registerRequest);

        assertEquals("alice@example.com", result.getEmail());
        verify(userRepository).existsByEmailIgnoreCase("alice@example.com");
    }

    @Test
    void createUser_shouldRejectDuplicateEmail() {
        RegisterRequest registerRequest = createRegisterRequest(
                "Alice",
                "  Alice@Example.COM  ",
                "password123"
        );

        when(userRepository.existsByEmailIgnoreCase("alice@example.com"))
                .thenReturn(true);

        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> userService.createUser(registerRequest)
        );

        assertEquals("Email already exists", exception.getMessage());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void createUser_shouldNotSaveWhenEmailIsDuplicate() {
        RegisterRequest registerRequest = createRegisterRequest(
                "Alice",
                "alice@example.com",
                "password123"
        );

        when(userRepository.existsByEmailIgnoreCase("alice@example.com"))
                .thenReturn(true);

        assertThrows(
                BusinessRuleException.class,
                () -> userService.createUser(registerRequest)
        );

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void getUserById_shouldReturnUserSuccessfully() {
        User user = createUser(1L, "Alice", "alice@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDto result = userService.getUserById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Alice", result.getName());
        assertEquals("alice@example.com", result.getEmail());
    }

    @Test
    void getUserById_shouldThrowWhenUserDoesNotExist() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.getUserById(999L)
        );

        assertEquals("User not found with id: 999", exception.getMessage());
    }

    @Test
    void getAllUsers_shouldReturnAllUsers() {
        User alice = createUser(1L, "Alice", "alice@example.com");
        User bob = createUser(2L, "Bob", "bob@example.com");

        when(userRepository.findAll()).thenReturn(List.of(alice, bob));

        List<UserDto> result = userService.getAllUsers();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("alice@example.com", result.get(0).getEmail());
        assertEquals(2L, result.get(1).getId());
        assertEquals("bob@example.com", result.get(1).getEmail());
    }

    @Test
    void updateUser_shouldUpdateUserSuccessfully() {
        User existingUser = createUser(1L, "Alice", "alice@example.com");
        UserDto updateDto = createUserDto("Alice Updated", "newalice@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmailIgnoreCaseAndIdNot(
                "newalice@example.com",
                1L
        )).thenReturn(false);
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        UserDto result = userService.updateUser(1L, updateDto);

        assertEquals("Alice Updated", result.getName());
        assertEquals("newalice@example.com", result.getEmail());
        assertEquals("Alice Updated", existingUser.getName());
        assertEquals("newalice@example.com", existingUser.getEmail());
        verify(userRepository).save(existingUser);
    }

    @Test
    void updateUser_shouldTrimAndLowercaseEmail() {
        User existingUser = createUser(1L, "Alice", "alice@example.com");
        UserDto updateDto = createUserDto("Alice", "  NewAlice@Example.COM  ");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmailIgnoreCaseAndIdNot(
                "newalice@example.com",
                1L
        )).thenReturn(false);
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        UserDto result = userService.updateUser(1L, updateDto);

        assertEquals("newalice@example.com", result.getEmail());
        verify(userRepository).existsByEmailIgnoreCaseAndIdNot(
                "newalice@example.com",
                1L
        );
    }

    @Test
    void updateUser_shouldRejectEmailBelongingToAnotherUser() {
        User existingUser = createUser(1L, "Alice", "alice@example.com");
        UserDto updateDto = createUserDto("Alice", "bob@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmailIgnoreCaseAndIdNot(
                "bob@example.com",
                1L
        )).thenReturn(true);

        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> userService.updateUser(1L, updateDto)
        );

        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_shouldAllowKeepingCurrentEmail() {
        User existingUser = createUser(1L, "Alice", "alice@example.com");
        UserDto updateDto = createUserDto("Alice Updated", "  Alice@Example.COM  ");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmailIgnoreCaseAndIdNot(
                "alice@example.com",
                1L
        )).thenReturn(false);
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        UserDto result = userService.updateUser(1L, updateDto);

        assertEquals("Alice Updated", result.getName());
        assertEquals("alice@example.com", result.getEmail());
        verify(userRepository).existsByEmailIgnoreCaseAndIdNot(
                "alice@example.com",
                1L
        );
        verify(userRepository).save(existingUser);
    }

    private UserDto createUserDto(String name, String email) {
        return new UserDto(null, name, email, Role.USER, null, null);
    }

    private User createUser(Long id, String name, String email) {
        return new User(
                id,
                name,
                email,
                "encoded-password",
                Role.USER,
                null,
                null
        );
    }

    private RegisterRequest createRegisterRequest(
            String name,
            String email,
            String password
    ) {
        return new RegisterRequest(name, email, password);
    }
}
