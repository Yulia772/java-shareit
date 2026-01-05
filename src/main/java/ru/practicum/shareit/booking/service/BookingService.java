package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.BookingState;

import java.util.List;

public interface BookingService {

    BookingResponseDto createBooking(Long bookerId, BookingRequestDto bookingRequestDto);

    BookingResponseDto updateBookingApproval(Long ownerId, Long bookingId, boolean approved);

    BookingResponseDto getBooking(Long userId, Long bookingId);

    List<BookingResponseDto> getBookingByBooker(Long bookerId, BookingState state, int from, int size);

    List<BookingResponseDto> getBookingByOwner(Long ownerId, BookingState state, int from, int size);

    void validateUserCanComment(Long userId, Long itemId);
}
