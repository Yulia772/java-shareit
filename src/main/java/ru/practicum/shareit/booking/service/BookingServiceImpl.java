package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.common.EntityFinder;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final EntityFinder entityFinder;

    @Override
    public BookingResponseDto createBooking(Long bookerId, BookingRequestDto bookingDto) {
        User booker = entityFinder.getUserOrThrow(bookerId);
        Item item = entityFinder.getItemOrThrow(bookingDto.getItemId());

        if (item.getOwner().getId().equals(bookerId)) {
            log.warn("Бронирование вещи владельцем id={} запрещено", bookerId);
            throw new ValidationException("Бронирование вещи владельцем запрещено");
        }
        if (!item.isAvailable()) {
            log.warn("Вещь id={} недоступна для бронирования", item.getId());
            throw new ValidationException("Вещь недоступна для бронирования");
        }

        validateBookingDates(bookingDto.getStart(), bookingDto.getEnd());
        Booking booking = BookingMapper.toBooking(bookingDto, item, booker);

        return BookingMapper.toBookingResponseDto(bookingRepository.save(booking));
    }

    @Override
    public BookingResponseDto updateBookingApproval(Long ownerId, Long bookingId, boolean approved) {
        Booking booking = entityFinder.getBookingOrThrow(bookingId);
        User owner = booking.getItem().getOwner();

        if (!owner.getId().equals(ownerId)) {
            log.warn("Пользователь id={} пытается подтвердить бронирование вещи, владельцем которой не является", ownerId);
            throw new ValidationException("Пользователь пытается подтвердить бронирование вещи, владельцем которой не является");
        }
        if (!booking.getStatus().equals(BookingStatus.WAITING)) {
            log.warn("Попытка повторно изменить статус бронирования id={}", bookingId);
            throw new ValidationException("Бронирование в данном статусе изменить нельзя");
        }

        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }
        Booking saved = bookingRepository.save(booking);
        return BookingMapper.toBookingResponseDto(saved);
    }

    @Override
    public BookingResponseDto getBooking(Long userId, Long bookingId){
        Booking booking = entityFinder.getBookingOrThrow(bookingId);
        Long bookerId = booking.getBooker().getId();
        Long ownerId = booking.getItem().getOwner().getId();

        if (!userId.equals(bookerId) && !userId.equals(ownerId)) {
            log.warn("Пользователь id={} пытается получить бронирование id={} не являясь ни владельцем, ни арендатором вещи", userId, bookingId);
            throw new NotFoundException("Пользователь не имеет прав на просмотр данного бронирования");
        }
        return BookingMapper.toBookingResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> getBookingByBooker(Long bookerId, BookingState state, int from, int size) {
        entityFinder.getUserOrThrow(bookerId);
        if (from < 0 || size <= 0) {
            log.warn("Некорректные параметры пагинации: from={} size={}", from, size);
            throw new ValidationException("Параметры пагинации должны быть: from >= 0, size > 0");
        }
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "start"));

        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;

        switch (state) {
            case ALL -> bookings = bookingRepository.findByBookerId(bookerId, pageable);
            case PAST -> bookings = bookingRepository.findByBookerIdAndEndBefore(bookerId, now, pageable);
            case CURRENT -> bookings = bookingRepository.findByBookerIdAndStartBeforeAndEndAfter(bookerId, now, now, pageable);
            case WAITING -> bookings = bookingRepository.findByBookerIdAndStatus(bookerId, BookingStatus.WAITING, pageable);
            case REJECTED -> bookings = bookingRepository.findByBookerIdAndStatus(bookerId, BookingStatus.REJECTED, pageable);
            case FUTURE -> bookings = bookingRepository.findByBookerIdAndStartAfter(bookerId, now, pageable);
            default -> throw new ValidationException("Unknown state: " + state);
        }
        return bookings.stream()
                .map(BookingMapper::toBookingResponseDto)
                .toList();
    }

    @Override
    public List<BookingResponseDto> getBookingByOwner(Long ownerId, BookingState state, int from, int size) {
        entityFinder.getUserOrThrow(ownerId);

        if (from < 0 || size <= 0) {
            log.warn("Некорректные параметры пагинации: from={} size={}", from, size);
            throw new ValidationException("Параметры пагинации должны быть: from >= 0, size > 0");
        }

        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "start"));

        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;
        switch (state) {
            case ALL -> bookings = bookingRepository.findByItemOwnerId(ownerId, pageable);
            case CURRENT -> bookings = bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfter(ownerId, now, now, pageable);
            case PAST -> bookings = bookingRepository.findByItemOwnerIdAndEndBefore(ownerId, now, pageable);
            case FUTURE -> bookings = bookingRepository.findByItemOwnerIdAndStartAfter(ownerId, now, pageable);
            case WAITING -> bookings = bookingRepository.findByItemOwnerIdAndStatus(ownerId, BookingStatus.WAITING, pageable);
            case REJECTED -> bookings = bookingRepository.findByItemOwnerIdAndStatus(ownerId, BookingStatus.REJECTED, pageable);
            default -> throw new ValidationException("Unknown state: " + state);
        }
        return bookings.stream()
                .map(BookingMapper::toBookingResponseDto)
                .toList();
    }

    @Override
    public void validateUserCanComment(Long userId, Long itemId) {
        boolean hasCompletedApprovedBooking = bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBefore(
                userId,
                itemId,
                BookingStatus.APPROVED,
                LocalDateTime.now()
        );
        if (!hasCompletedApprovedBooking) {
            log.warn("Пользователь id={} пытается оставить комментарий к вещи id={}, не имея подтвержденного завершенного бронирования", userId, itemId);
            throw new ValidationException("Оставлять комментарий можно только после завершенного бронирования");
        }
    }

    private void validateBookingDates(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            log.warn("Не указаны даты бронирования: start={} end={}", start, end);
            throw new ValidationException("Дата начала и окончания бронирования должны быть указаны");
        }
        if (!start.isBefore(end)) {
            log.warn("Некорректный интервал бронирования: start={} end={}", start, end);
            throw new ValidationException("Дата начала бронирования должна быть раньше окончания");
        }
        LocalDateTime now = LocalDateTime.now();
        if (start.isBefore(now) || end.isBefore(now)) {
            log.warn("Попытка создать бронирование в прошлом: start={} end={} now={}", start, end, now);
            throw new ValidationException("Нельзя создать бронирование в прошлом");
        }
    }
}

