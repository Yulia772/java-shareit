package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.constants.HeaderConstants;
import ru.practicum.shareit.exception.ValidationException;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@AllArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    //Создать бронирование
    @PostMapping
    public ResponseEntity<BookingResponseDto> createBooking(
            @RequestHeader(HeaderConstants.USER_ID) Long bookerId,
            @RequestBody BookingRequestDto bookingDto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.createBooking(bookerId, bookingDto));
    }

    //Подтвердить / отклонить бронирование
    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDto> updateBookingApproval(
            @RequestHeader(HeaderConstants.USER_ID) Long ownerId,
            @PathVariable Long bookingId,
            @RequestParam("approved") boolean approved
    ) {
        return ResponseEntity.ok(
                bookingService.updateBookingApproval(ownerId, bookingId, approved)
        );
    }

    // Получить одно бронирование
    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDto> getBooking(
            @RequestHeader(HeaderConstants.USER_ID) Long userId,
            @PathVariable Long bookingId
    ) {
        return ResponseEntity.ok(
                bookingService.getBooking(userId, bookingId)
        );
    }

    // Все бронирования пользователя как букера
    @GetMapping
    public ResponseEntity<List<BookingResponseDto>> getBookingsForBooker(
            @RequestHeader(HeaderConstants.USER_ID) Long userId,
            @RequestParam(name = "state", defaultValue = "ALL") String state,
            @RequestParam(name = "from", defaultValue = "0") int from,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        BookingState bookingState = parseState(state);
        return ResponseEntity.ok(
                bookingService.getBookingByBooker(userId, bookingState, from, size)
        );
    }

    // Все бронирования вещей владельца
    @GetMapping("/owner")
    public ResponseEntity<List<BookingResponseDto>> getBookingsForOwner(
            @RequestHeader(HeaderConstants.USER_ID) Long userId,
            @RequestParam(name = "state", defaultValue = "ALL") String state,
            @RequestParam(name = "from", defaultValue = "0") int from,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        BookingState bookingState = parseState(state);
        return ResponseEntity.ok(
                bookingService.getBookingByOwner(userId, bookingState, from, size)
        );
    }

    private BookingState parseState(String state) {
        try {
            return BookingState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Unknown state: " + state);
        }
    }
}