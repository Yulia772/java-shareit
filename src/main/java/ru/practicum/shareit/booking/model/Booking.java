package ru.practicum.shareit.booking.model;

import lombok.Data;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.booking.enums.BookingStatus;
import java.time.LocalDateTime;

@Data
public class Booking {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private Item item;
    private User booker;
    private BookingStatus status;

}
