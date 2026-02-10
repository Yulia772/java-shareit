package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NewUserRequestDto {

    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;
}
