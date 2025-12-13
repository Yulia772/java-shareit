package ru.practicum.shareit.request.model;

import lombok.Data;

import java.time.LocalDateTime;
import ru.practicum.shareit.user.model.User;

@Data
public class ItemRequest {
    private Long id;
    private String description;
    private User requestor;
    private LocalDateTime created;
}
