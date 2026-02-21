package ru.practicum.shareit.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.repository.ItemRequestRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EntityFinderTest {

    @Mock
    ItemRepository itemRepository;

    @Mock
    BookingRepository bookingRepository;

    @Mock
    ItemRequestRepository itemRequestRepository;

    @InjectMocks
    EntityFinder entityFinder;

    @Test
    void getItemOrThrow_whenItemNotFound_shouldThrow() {
        Long itemId = 1L;

        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> entityFinder.getItemOrThrow(itemId));

        verify(itemRepository).findById(itemId);
        verifyNoMoreInteractions(itemRepository);
        verifyNoInteractions(bookingRepository, itemRequestRepository);
    }

    @Test
    void getBookingOrThrow_whenBookingNotFound_shouldThrow() {
        Long bookingId = 2L;

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> entityFinder.getBookingOrThrow(bookingId));

        verify(bookingRepository).findById(bookingId);
        verifyNoMoreInteractions(bookingRepository);
        verifyNoInteractions(itemRepository, itemRequestRepository);
    }

    @Test
    void getItemRequestOrThrow_whenRequestNotFound_shouldThrow() {
        Long requestId = 3L;

        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> entityFinder.getItemRequestOrThrow(requestId));

        verify(itemRequestRepository).findById(requestId);
        verifyNoMoreInteractions(itemRequestRepository);
        verifyNoInteractions(itemRepository, bookingRepository);
    }
}