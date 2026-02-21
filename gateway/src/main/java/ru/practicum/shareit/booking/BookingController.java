package ru.practicum.shareit.booking;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.constants.HeaderConstants;


@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
	private final BookingClient bookingClient;

	@GetMapping
	public ResponseEntity<Object> getBookingsForBooker(@RequestHeader(HeaderConstants.USER_ID) long userId,
			@RequestParam(name = "state", defaultValue = "all") String stateParam,
			@PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
			@Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
		BookingState state = BookingState.from(stateParam)
				.orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
		log.info("Get booking with state {}, userId={}, from={}, size={}", stateParam, userId, from, size);
		return bookingClient.getBookingsForBooker(userId, state, from, size);
	}

	@PostMapping
	public ResponseEntity<Object> createBooking(@RequestHeader(HeaderConstants.USER_ID) long userId,
			@RequestBody @Valid BookItemRequestDto requestDto) {
		log.info("Creating booking {}, userId={}", requestDto, userId);
		return bookingClient.createBooking(userId, requestDto);
	}

	@GetMapping("/{bookingId}")
	public ResponseEntity<Object> getBooking(@RequestHeader(HeaderConstants.USER_ID) long userId,
			@PathVariable Long bookingId) {
		log.info("Get booking {}, userId={}", bookingId, userId);
		return bookingClient.getBooking(userId, bookingId);
	}

	@GetMapping("/owner")
	public ResponseEntity<Object> getBookingsForOwner(@RequestHeader(HeaderConstants.USER_ID) long userId,
													   @RequestParam(name = "state", defaultValue = "all") String stateParam,
													   @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
													   @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
		BookingState state = BookingState.from(stateParam)
				.orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
		log.info("Get booking for owner with state {}, userId={}, from={}, size={}", stateParam, userId, from, size);
		return bookingClient.getBookingsForOwner(userId, state, from, size);
	}

	@PatchMapping("/{bookingId}")
	public ResponseEntity<Object> updateBookingApproval(
			@RequestHeader(HeaderConstants.USER_ID) long ownerId,
			@PathVariable long bookingId,
			@RequestParam("approved") boolean approved
	) {
		log.info("Update booking approval bookingId={}, ownerId={}, approved ={}", bookingId, ownerId, approved);
		return bookingClient.updateBookingApproval(ownerId, bookingId, approved);
	}
}
