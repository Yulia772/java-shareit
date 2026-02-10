package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserServiceImpl userService;

    private UserDto dto;

    @BeforeEach
    void setUp() {
        dto = new UserDto();
        dto.setName("User");
        dto.setEmail("user@mail.com");
    }

    @Test
    void createUser_ok() {
        when(userRepository.existsByEmail(dto.getEmail()))
                        .thenReturn(false);

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setName(dto.getName());
        savedUser.setEmail(dto.getEmail());

        when(userRepository.save(Mockito.any(User.class)))
                .thenReturn(savedUser);
        UserDto result = userService.createUser(dto);

        assertEquals(1L, result.getId());
        assertEquals(dto.getName(), result.getName());
        assertEquals(dto.getEmail(), result.getEmail());

        verify(userRepository, times(1)).existsByEmail(dto.getEmail());
        verify(userRepository, times(1)).save(Mockito.any(User.class));

    }

    @Test
    void createUser_whenDuplicateEmail() {
        when(userRepository.existsByEmail(dto.getEmail()))
                .thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class,
                () -> userService.createUser(dto));
        verify(userRepository, times(1)).existsByEmail(dto.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_whenBadEmailFormat() {
        dto.setEmail(null);
        assertThrows(ValidationException.class,
                () -> userService.createUser(dto));
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_whenEmailBlank_shouldThrowValidation() {
        dto.setEmail(" ");

        assertThrows(ValidationException.class, () -> userService.createUser(dto));

        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_whenEmailContainsSpaces_shouldThrowValidation() {
        dto.setEmail("user @mail.com");

        assertThrows(ValidationException.class, () -> userService.createUser(dto));

        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_whenEmailHasNoAt_shouldThrowValidation() {
        dto.setEmail("usermail.com");

        assertThrows(ValidationException.class, () -> userService.createUser(dto));

        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_whenEmailHasTwoAt_shouldThrowValidation() {
        dto.setEmail("user@@mail.com");

        assertThrows(ValidationException.class, () -> userService.createUser(dto));

        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_whenEmailDomainHasNoDot_shouldThrowValidation() {
        dto.setEmail("user@mail");

        assertThrows(ValidationException.class, () -> userService.createUser(dto));

        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_whenEmailDomainEndsWithDot_shouldThrowValidation() {
        dto.setEmail("user@mail.");

        assertThrows(ValidationException.class, () -> userService.createUser(dto));

        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_ok_updatesNameAndEmail() {
        Long userId = 1L;
        User oldUser = new User();
        oldUser.setId(userId);
        oldUser.setName("Old Name");
        oldUser.setEmail("old@mail.com");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(oldUser));

        when(userRepository.existsByEmail(dto.getEmail()))
                .thenReturn(false);
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = userService.updateUser(userId, dto);

        assertEquals(userId, result.getId());
        assertEquals(dto.getName(), result.getName());
        assertEquals(dto.getEmail(), result.getEmail());

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).existsByEmail(dto.getEmail());
        verify(userRepository, times(1)).save(Mockito.any(User.class));

    }

    @Test
    void updateUser_ok_updatesNameWithoutEmail() {
        Long userId = 1L;
        User oldUser = new User();
        oldUser.setId(userId);
        oldUser.setName("Old Name");
        oldUser.setEmail("same@mail.com");

        dto.setEmail(null);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(oldUser));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = userService.updateUser(userId, dto);

        assertEquals(userId, result.getId());
        assertEquals(dto.getName(), result.getName());
        assertEquals(oldUser.getEmail(), result.getEmail());

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository,never()).existsByEmail(anyString());
        verify(userRepository, times(1)).save(Mockito.any(User.class));
    }

    @Test
    void updateUser_whenUserNotFound() {
        Long userId = 1L;

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> userService.updateUser(userId, dto));
        verify(userRepository, never()).save(any());
        verify(userRepository,never()).existsByEmail(anyString());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void updateUser_whenNewEmailAlreadyExists() {
        Long userId = 1L;
        User oldUser = new User();
        oldUser.setId(userId);
        oldUser.setName("User");
        oldUser.setEmail("old@mail.com");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(oldUser));
        when(userRepository.existsByEmail(dto.getEmail()))
                .thenReturn(true);
        assertThrows(EmailAlreadyExistsException.class,
                () -> userService.updateUser(userId, dto));

        verify(userRepository, times(1)).existsByEmail(dto.getEmail());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any());
    }

    @Test
    void getUser_ok() {
        Long userId = 1L;
        User oldUser = new User();
        oldUser.setId(userId);
        oldUser.setName("OldUser");
        oldUser.setEmail("old@mail.com");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(oldUser));
        UserDto result = userService.getUser(userId);
        assertEquals(userId, result.getId());
        assertEquals(oldUser.getName(), result.getName());
        assertEquals(oldUser.getEmail(), result.getEmail());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void getUser_whenUserNotFound() {
        Long userId = 1L;

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> userService.getUser(userId));
        verify(userRepository, times(1)).findById(userId);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getAllUsers_ok() {
        Long userId = 1L;
        User oldUser = new User();
        oldUser.setId(userId);
        oldUser.setName("OldUser");
        oldUser.setEmail("old@mail.com");

        when(userRepository.findAll())
                .thenReturn(List.of(oldUser));

        List<UserDto> result = userService.getAllUsers();

        assertThat(result.size(), equalTo(1));
        assertThat(result.get(0).getId(), equalTo(userId));
        assertThat(result.get(0).getName(), equalTo(oldUser.getName()));
        assertThat(result.get(0).getEmail(), equalTo(oldUser.getEmail()));
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void deleteUser_ok() {
        Long userId = 1L;
        User oldUser = new User();
        oldUser.setId(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(oldUser));
        userService.deleteUser(userId);
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).deleteById(userId);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void deleteUser_whenUserNotFound() {
        Long userId = 1L;

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> userService.deleteUser(userId));
        verify(userRepository, never()).deleteById(any());
        verify(userRepository, times(1)).findById(userId);
    }
}
