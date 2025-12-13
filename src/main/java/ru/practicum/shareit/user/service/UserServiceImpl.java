package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto createUser(UserDto userDto) {
        validateEmailFormat(userDto.getEmail());
        validateEmailUnique(userDto.getEmail());
        User user = UserMapper.toUser(userDto);
        User savedUser = userRepository.save(user);
        UserDto savedDto = UserMapper.toUserDto(savedUser);
        return savedDto;
    }

    @Override
    public UserDto updateUser(Long id, UserDto userDto) {
        User oldUser = getUserOrThrow(id);
        String newName = userDto.getName();
        if(newName != null) {
            oldUser.setName(newName);
        }

        String newEmail = userDto.getEmail();

        if(newEmail != null) {
            String oldEmail = oldUser.getEmail();
            if(!Objects.equals(oldEmail, newEmail)) {
                validateEmailFormat(newEmail);
                validateEmailUnique(newEmail);
                oldUser.setEmail(newEmail);
            }
        }
        User saveUser = userRepository.update(oldUser);
        UserDto updateUserDto = UserMapper.toUserDto(saveUser);
        return updateUserDto;

    }

    @Override
    public UserDto getUser(Long id) {
        User getById = getUserOrThrow(id);
        UserDto findById = UserMapper.toUserDto(getById);
        return findById;
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    public void deleteUser(Long id) {
        getUserOrThrow(id);
        userRepository.deleteById(id);
    }

    private void validateEmailUnique(String email) {
        if(userRepository.existsByEmail(email)) {
            log.warn("Пользователь с таким email={} уже существует", email);
            throw new EmailAlreadyExistsException("Пользователь с таким email уже существует");
        }
    }

    private User getUserOrThrow(Long id) {
        User existingUser = userRepository.findById(id);
        if(existingUser == null) {
            log.warn("Пользователя с таким id={} не существует", id);
            throw new NotFoundException("Пользователя с таким id не существует");
        }
        return existingUser;
    }

    private void validateEmailFormat(String email) {
        if (email == null || email.isBlank()) {
            log.warn("Передан пустой email");
            throw new ValidationException("Email не может быть пустым");
        }

        if (email.contains(" ")) {
            log.warn("В email={} есть пробелы", email);
            throw new ValidationException("Email не должен содержать пробелов");
        }

        int atIndex = email.indexOf('@');
        if (atIndex <= 0 || atIndex != email.lastIndexOf('@') || atIndex == email.length() - 1) {
            log.warn("Передан некорректный email={} (проблема с '@')", email);
            throw new ValidationException("Некорректный email");
        }

        String domainPart = email.substring(atIndex + 1);
        int dotIndex = domainPart.lastIndexOf('.');
        if (dotIndex <= 0 || dotIndex == domainPart.length() - 1) {
            log.warn("Передан некорректный email={} (проблема с доменом)", email);
            throw new ValidationException("Некорректный email");
        }
    }
}
