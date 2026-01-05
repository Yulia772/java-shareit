package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerId(Long bookerId, Pageable pageable);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfter(
            Long bookerId,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    List<Booking> findByBookerIdAndEndBefore(
            Long bookerId,
            LocalDateTime now,
            Pageable pageable
    );

    List<Booking> findByBookerIdAndStartAfter(
            Long bookerId,
            LocalDateTime now,
            Pageable pageable
    );

    List<Booking> findByBookerIdAndStatus(
            Long bookerId,
            BookingStatus status,
            Pageable pageable
    );

    List<Booking> findByItemOwnerId(Long ownerId, Pageable pageable);

    List<Booking> findByItemOwnerIdAndStartBeforeAndEndAfter(
            Long ownerId,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    List<Booking> findByItemOwnerIdAndEndBefore(
            Long ownerId,
            LocalDateTime now,
            Pageable pageable
    );

    List<Booking> findByItemOwnerIdAndStartAfter(
            Long ownerId,
            LocalDateTime now,
            Pageable pageable
    );

    List<Booking> findByItemOwnerIdAndStatus(
            Long ownerId,
            BookingStatus status,
            Pageable pageable
    );

    Optional<Booking> findFirstByItemIdAndEndBeforeAndStatusOrderByEndDesc(
            Long itemId,
            LocalDateTime now,
            BookingStatus status
    );

    Optional<Booking> findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(
            Long itemId,
            LocalDateTime now,
            BookingStatus status
    );

    List<Booking> findByItemIdInAndEndBeforeAndStatusOrderByEndDesc(
            List<Long> itemIds,
            LocalDateTime end,
            BookingStatus status
    );

    List<Booking> findByItemIdInAndStartAfterAndStatusOrderByStartAsc(
            List<Long> itemIds,
            LocalDateTime start,
            BookingStatus status
    );

    boolean existsByBookerIdAndItemIdAndStatusAndEndBefore(
            Long bookerId,
            Long itemId,
            BookingStatus status,
            LocalDateTime now
    );
}
