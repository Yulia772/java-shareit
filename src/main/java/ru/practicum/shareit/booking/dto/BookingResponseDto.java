package ru.practicum.shareit.booking.dto;

import lombok.Data;
import ru.practicum.shareit.booking.enums.BookingStatus;

import java.time.LocalDateTime;

@Data
public class BookingResponseDto {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private BookingStatus status;
    private BookerShortDto booker;
    private ItemShortDto item;

    @Data
    public static class BookerShortDto {
        private Long id;
    }

    @Data
    public static class ItemShortDto {
        private Long id;
        private String name;
    }
}
