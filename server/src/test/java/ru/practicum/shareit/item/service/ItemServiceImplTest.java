package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.common.EntityFinder;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceImplTest {

    @Mock
    ItemRepository itemRepository;

    @Mock
    ItemRequestRepository itemRequestRepository;

    @Mock
    BookingRepository bookingRepository;

    @Mock
    CommentRepository commentRepository;

    @Mock
    EntityFinder entityFinder;

    @InjectMocks
    ItemServiceImpl itemService;

    private ItemDto dto;
    private User owner;
    private User booker;
    private User author;

    private ItemRequest itemRequest;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setName("Owner");
        owner.setEmail("owner@mail.com");

        booker = new User();
        booker.setId(5L);
        booker.setName("Booker");

        author = new User();
        author.setId(7L);
        author.setName("Author");

        dto = new ItemDto();
        dto.setName("Drill");
        dto.setDescription("Powerful drill");
        dto.setAvailable(true);

        itemRequest = new ItemRequest();
    }

    @Test
    void createItem_withoutRequestId_ok() {
        mockOwnerExists();

        when(itemRepository.save(any(Item.class)))
                .thenAnswer(invocation -> {
                    Item item = invocation.getArgument(0);
                    item.setId(10L);
                    return item;
                });

        ItemDto result = itemService.createItem(owner.getId(), dto);

        assertEquals(10L, result.getId());
        assertEquals(dto.getName(), result.getName());
        assertEquals(dto.getDescription(), result.getDescription());
        assertEquals(dto.getAvailable(), result.getAvailable());
        // requestId не передавали, должно быть null
        assertNull(result.getRequestId());

        verify(entityFinder, times(1)).getUserOrThrow(owner.getId());
        verify(itemRepository, times(1)).save(any(Item.class));
        verifyNoInteractions(itemRequestRepository);
    }

    @Test
    void createItem_withRequestId_ok() {
        dto.setRequestId(100L);
        itemRequest.setId(100L);

        mockOwnerExists();

        // сервис берёт запрос вещи прямо из itemRequestRepository
        when(itemRequestRepository.findById(dto.getRequestId()))
                .thenReturn(Optional.of(itemRequest));

        when(itemRepository.save(any(Item.class)))
                .thenAnswer(invocation -> {
                    Item item = invocation.getArgument(0);
                    item.setId(10L);
                    return item;
                });

        ItemDto result = itemService.createItem(owner.getId(), dto);

        assertEquals(10L, result.getId());
        assertEquals(dto.getName(), result.getName());
        assertEquals(dto.getDescription(), result.getDescription());
        assertEquals(dto.getAvailable(), result.getAvailable());
        // в ItemMapper requestId берётся из item.getRequest().getId()
        assertEquals(dto.getRequestId(), result.getRequestId());

        verify(entityFinder, times(1)).getUserOrThrow(owner.getId());
        verify(itemRequestRepository, times(1)).findById(dto.getRequestId());
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void createItem_thenAvailableIsNull_whenThrowsValidationException() {
        dto.setAvailable(null);
        mockOwnerExists();

        assertThrows(ValidationException.class,
                () -> itemService.createItem(owner.getId(), dto));

        // при ошибке валидации до репозитория мы не доходим
        verifyNoInteractions(itemRepository);
    }

    @Test
    void createItem_thenOwnerNotFound_whenThrowsNotFoundException() {
        when(entityFinder.getUserOrThrow(owner.getId()))
                .thenThrow(new NotFoundException("Пользователь не найден"));

        assertThrows(NotFoundException.class,
                () -> itemService.createItem(owner.getId(), dto));

        verify(entityFinder, times(1)).getUserOrThrow(owner.getId());
        verify(itemRepository, never()).save(any());
    }

    @Test
    void createItem_whenNameIsNull() {
        dto.setName(null);
        mockOwnerExists();

        assertThrows(ValidationException.class,
                () -> itemService.createItem(owner.getId(), dto));
        verifyNoInteractions(itemRepository);
    }

    @Test
    void createItem_whenNameIsEmpty() {
        dto.setName(" ");
        mockOwnerExists();

        assertThrows(ValidationException.class,
                () -> itemService.createItem(owner.getId(), dto));
        verifyNoInteractions(itemRepository);
    }

    @Test
    void createItem_whenDescriptionIsNull() {
        dto.setDescription(null);
        mockOwnerExists();

        assertThrows(ValidationException.class,
                () -> itemService.createItem(owner.getId(), dto));
        verifyNoInteractions(itemRepository);
    }

    @Test
    void createItem_whenDescriptionIsEmpty() {
        dto.setDescription(" ");
        mockOwnerExists();

        assertThrows(ValidationException.class,
                () -> itemService.createItem(owner.getId(), dto));
        verifyNoInteractions(itemRepository);
    }

    @Test
    void updateItem_ok() {
        Long ownerId = owner.getId();
        Long itemId = 100L;

        Item item = createItem(itemId, "OldDrill", "Old description");
        item.setAvailable(false);

        when(entityFinder.getUserOrThrow(ownerId))
                .thenReturn(owner);
        when(entityFinder.getItemOrThrow(itemId))
                .thenReturn(item);
        when(itemRepository.save(any(Item.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ItemDto result = itemService.updateItem(ownerId, itemId, dto);

        assertEquals(itemId, result.getId());
        assertEquals(dto.getName(), result.getName());
        assertEquals(dto.getDescription(), result.getDescription());
        assertEquals(dto.getAvailable(), result.getAvailable());
        assertEquals(dto.getRequestId(), result.getRequestId());

        verify(entityFinder, times(1)).getUserOrThrow(ownerId);
        verify(entityFinder, times(1)).getItemOrThrow(itemId);
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void updateItem_whenUserIsNotOwner_shouldThrowNotFound() {
        // владелец вещи — другой пользователь
        User anotherUser = new User();
        anotherUser.setId(2L);

        Item item = new Item();
        item.setId(100L);
        item.setOwner(anotherUser);

        when(entityFinder.getUserOrThrow(owner.getId()))
                .thenReturn(owner);
        when(entityFinder.getItemOrThrow(item.getId()))
                .thenReturn(item);

        assertThrows(NotFoundException.class,
                () -> itemService.updateItem(owner.getId(), item.getId(), dto));

        verify(entityFinder, times(1)).getUserOrThrow(owner.getId());
        verify(entityFinder, times(1)).getItemOrThrow(item.getId());
        verify(itemRepository, never()).save(any());
    }

    @Test
    void updateItem_whenItemNotFoundById() {
        Long userId = 100L;
        Long itemId = 999L;

        User user = new User();
        user.setId(userId);

        when(entityFinder.getUserOrThrow(userId))
                .thenReturn(user);
        when(entityFinder.getItemOrThrow(itemId))
                .thenThrow(new NotFoundException("Вещь не найдена"));

        assertThrows(NotFoundException.class,
                () -> itemService.updateItem(userId, itemId, dto));

        verify(entityFinder, times(1)).getUserOrThrow(userId);
        verify(entityFinder, times(1)).getItemOrThrow(itemId);
        verify(itemRepository, never()).save(any());
    }

    @Test
    void getItem() {
        Long ownerId = owner.getId();
        Long itemId = 10L;

        Item item = createItem(itemId, "Drill", "Powerful drill");
        Booking lastBooking = createPastBooking(1L, item, 1);
        Booking nextBooking = createFutureBooking(2L, item, 1);
        Comment comment = createComment(100L, item, "Отличная дрель");

        when(entityFinder.getItemOrThrow(itemId))
                .thenReturn(item);
        when(bookingRepository.findFirstByItemIdAndEndBeforeAndStatusOrderByEndDesc(
                eq(itemId),
                any(LocalDateTime.class),
                eq(BookingStatus.APPROVED)))
                .thenReturn(Optional.of(lastBooking));

        when(bookingRepository.findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(
                eq(itemId),
                any(LocalDateTime.class),
                eq(BookingStatus.APPROVED)))
                .thenReturn(Optional.of(nextBooking));

        when(commentRepository.findByItemIdOrderByCreatedDesc(itemId))
                .thenReturn(List.of(comment));

        ItemWithBookingsDto result = itemService.getItem(ownerId, itemId);

        assertEquals(itemId, result.getId());
        assertEquals(item.getName(), result.getName());
        assertEquals(item.getDescription(), result.getDescription());
        assertEquals(item.isAvailable(), result.getAvailable());

        assertEquals(lastBooking.getId(), result.getLastBooking().getId());
        assertEquals(nextBooking.getId(), result.getNextBooking().getId());

        assertEquals(1, result.getComments().size());
        assertEquals(comment.getText(), result.getComments().get(0).getText());

        verify(bookingRepository, times(1))
                .findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(
                        eq(itemId),
                        any(LocalDateTime.class),
                        eq(BookingStatus.APPROVED)
                );
        verify(bookingRepository, times(1))
                .findFirstByItemIdAndEndBeforeAndStatusOrderByEndDesc(
                        eq(itemId),
                        any(LocalDateTime.class),
                        eq(BookingStatus.APPROVED)
                );
        verify(commentRepository, times(1))
                .findByItemIdOrderByCreatedDesc(itemId);
    }

    @Test
    void getItem_whenNotOwner() {
        Long itemId = 10L;

        User notOwner = new User();
        notOwner.setId(99L);

        Item item = createItem(itemId, "Drill", "Powerful drill");
        Comment comment = createComment(100L, item, "Отличная дрель");

        when(entityFinder.getItemOrThrow(itemId))
                .thenReturn(item);

        when(commentRepository.findByItemIdOrderByCreatedDesc(itemId))
                .thenReturn(List.of(comment));

        ItemWithBookingsDto result = itemService.getItem(notOwner.getId(), itemId);

        assertEquals(itemId, result.getId());
        assertEquals(item.getName(), result.getName());
        assertEquals(item.getDescription(), result.getDescription());
        assertEquals(item.isAvailable(), result.getAvailable());

        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());

        assertEquals(1, result.getComments().size());
        assertEquals(comment.getText(), result.getComments().get(0).getText());

        verify(entityFinder, times(1))
                .getItemOrThrow(itemId);
        verify(commentRepository, times(1))
                .findByItemIdOrderByCreatedDesc(itemId);
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void getItem_whenItemNotFound() {
        Long itemId = 100L;
        Long userId = 99L;
        when(entityFinder.getItemOrThrow(itemId))
                .thenThrow(new NotFoundException("Вещь не найдена"));

        assertThrows(NotFoundException.class,
                () -> itemService.getItem(userId, itemId));
        verifyNoInteractions(bookingRepository);
        verifyNoInteractions(commentRepository);
    }

    @Test
    void getItem_whenNoComments() {
        Long ownerId = owner.getId();
        Long itemId = 10L;

        Item item = createItem(itemId, "Drill", "Powerful drill");
        Booking lastBooking = createPastBooking(1L, item, 1);
        Booking nextBooking = createFutureBooking(2L, item, 1);

        when(entityFinder.getItemOrThrow(itemId))
                .thenReturn(item);
        when(bookingRepository.findFirstByItemIdAndEndBeforeAndStatusOrderByEndDesc(
                eq(itemId),
                any(LocalDateTime.class),
                eq(BookingStatus.APPROVED)))
                .thenReturn(Optional.of(lastBooking));

        when(bookingRepository.findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(
                eq(itemId),
                any(LocalDateTime.class),
                eq(BookingStatus.APPROVED)))
                .thenReturn(Optional.of(nextBooking));

        when(commentRepository.findByItemIdOrderByCreatedDesc(itemId))
                .thenReturn(List.of());

        ItemWithBookingsDto result = itemService.getItem(ownerId, itemId);

        assertEquals(itemId, result.getId());
        assertEquals(item.getName(), result.getName());
        assertEquals(item.getDescription(), result.getDescription());
        assertEquals(item.isAvailable(), result.getAvailable());

        assertEquals(lastBooking.getId(), result.getLastBooking().getId());
        assertEquals(nextBooking.getId(), result.getNextBooking().getId());

        assertEquals(0, result.getComments().size());

        verify(bookingRepository, times(1))
                .findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(
                        eq(itemId),
                        any(LocalDateTime.class),
                        eq(BookingStatus.APPROVED)
                );
        verify(bookingRepository, times(1))
                .findFirstByItemIdAndEndBeforeAndStatusOrderByEndDesc(
                        eq(itemId),
                        any(LocalDateTime.class),
                        eq(BookingStatus.APPROVED)
                );
        verify(commentRepository, times(1))
                .findByItemIdOrderByCreatedDesc(itemId);
        verify(entityFinder, times(1))
                .getItemOrThrow(itemId);
        verifyNoMoreInteractions(bookingRepository, commentRepository, entityFinder);
    }

    @Test
    void getItem_whenNoBookings() {
        Long ownerId = owner.getId();
        Long itemId = 10L;

        Item item = createItem(itemId, "Drill", "Powerful drill");
        Comment comment = createComment(100L, item, "Отличная дрель");

        when(entityFinder.getItemOrThrow(itemId))
                .thenReturn(item);
        when(bookingRepository.findFirstByItemIdAndEndBeforeAndStatusOrderByEndDesc(
                eq(itemId),
                any(LocalDateTime.class),
                eq(BookingStatus.APPROVED)))
                .thenReturn(Optional.empty());

        when(bookingRepository.findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(
                eq(itemId),
                any(LocalDateTime.class),
                eq(BookingStatus.APPROVED)))
                .thenReturn(Optional.empty());

        when(commentRepository.findByItemIdOrderByCreatedDesc(itemId))
                .thenReturn(List.of(comment));

        ItemWithBookingsDto result = itemService.getItem(ownerId, itemId);

        assertEquals(itemId, result.getId());
        assertEquals(item.getName(), result.getName());
        assertEquals(item.getDescription(), result.getDescription());
        assertEquals(item.isAvailable(), result.getAvailable());

        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());

        assertEquals(1, result.getComments().size());

        verify(bookingRepository, times(1))
                .findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(
                        eq(itemId),
                        any(LocalDateTime.class),
                        eq(BookingStatus.APPROVED)
                );
        verify(bookingRepository, times(1))
                .findFirstByItemIdAndEndBeforeAndStatusOrderByEndDesc(
                        eq(itemId),
                        any(LocalDateTime.class),
                        eq(BookingStatus.APPROVED)
                );
        verify(commentRepository, times(1))
                .findByItemIdOrderByCreatedDesc(itemId);
    }

    @Test
    void getItemsByOwner_ok() {
        Long userId = owner.getId();
        Long itemId1 = 10L;
        Long itemId2 = 100L;

        // Вещи владельца
        Item item1 = createItem(itemId1, "Drill", "Powerful drill");
        Item item2 = createItem(itemId2, "Mixer", "Wonderful mixer");

        // Прошлые бронирования для обеих вещей
        Booking lastBookingItem1 = createPastBooking(1L, item1, 1);
        Booking lastBookingItem2 = createPastBooking(3L, item2, 2);

        // Будущее бронирование только для первой вещи
        Booking nextBookingItem1 = createFutureBooking(2L, item1, 1);
        Comment comment1 = createComment(100L, item1, "Отличная дрель");
        Comment comment2 = createComment(99L, item2, "Замечательный миксер");

        when(entityFinder.getUserOrThrow(userId))
                .thenReturn(owner);

        // Возвращаем две вещи этого владельца
        when(itemRepository.findAllByOwnerId(userId))
                .thenReturn(List.of(item1, item2));

        // запрос прошлых бронирований по двум вещам
        when(bookingRepository.findByItemIdInAndEndBeforeAndStatusOrderByEndDesc(
                anyList(),
                any(LocalDateTime.class),
                eq(BookingStatus.APPROVED)))
                .thenReturn(List.of(lastBookingItem1, lastBookingItem2));

        // запрос будущих бронирований по двум вещам
        when(bookingRepository.findByItemIdInAndStartAfterAndStatusOrderByStartAsc(
                anyList(),
                any(LocalDateTime.class),
                eq(BookingStatus.APPROVED)))
                .thenReturn(List.of(nextBookingItem1)); // только для item1

        // запрос комментариев ко всем вещам
        when(commentRepository.findByItemIdInOrderByCreatedDesc(anyList()))
                .thenReturn(List.of(comment1, comment2));

        // Вызов сервиса
        List<ItemWithBookingsDto> result = itemService.getItemsByOwner(userId);

        // Проверки
        assertEquals(2, result.size());

        ItemWithBookingsDto dto1 = result.get(0);
        ItemWithBookingsDto dto2 = result.get(1);

        // первая вещь
        assertEquals(itemId1, dto1.getId());
        assertEquals(item1.getName(), dto1.getName());
        assertEquals(item1.getDescription(), dto1.getDescription());
        assertEquals(item1.isAvailable(), dto1.getAvailable());

        assertNotNull(dto1.getLastBooking());
        assertEquals(lastBookingItem1.getId(), dto1.getLastBooking().getId());
        assertEquals(booker.getId(), dto1.getLastBooking().getBookerId());

        assertNotNull(dto1.getNextBooking());
        assertEquals(nextBookingItem1.getId(), dto1.getNextBooking().getId());
        assertEquals(booker.getId(), dto1.getNextBooking().getBookerId());

        assertEquals(1, dto1.getComments().size());
        assertEquals(comment1.getText(), dto1.getComments().get(0).getText());

        // вторая вещь
        assertEquals(itemId2, dto2.getId());
        assertEquals(item2.getName(), dto2.getName());
        assertEquals(item2.getDescription(), dto2.getDescription());
        assertEquals(item2.isAvailable(), dto2.getAvailable());

        assertNotNull(dto2.getLastBooking());
        assertEquals(lastBookingItem2.getId(), dto2.getLastBooking().getId());
        assertEquals(booker.getId(), dto2.getLastBooking().getBookerId());

        assertNull(dto2.getNextBooking());

        assertEquals(1, dto2.getComments().size());
        assertEquals(comment2.getText(), dto2.getComments().get(0).getText());

        verify(entityFinder, times(1)).getUserOrThrow(userId);
        verify(itemRepository, times(1)).findAllByOwnerId(userId);
        verify(bookingRepository, times(1))
                .findByItemIdInAndEndBeforeAndStatusOrderByEndDesc(anyList(), any(LocalDateTime.class), eq(BookingStatus.APPROVED));
        verify(bookingRepository, times(1))
                .findByItemIdInAndStartAfterAndStatusOrderByStartAsc(anyList(), any(LocalDateTime.class), eq(BookingStatus.APPROVED));
        verify(commentRepository, times(1))
                .findByItemIdInOrderByCreatedDesc(anyList());
    }

    @Test
    void getItemsByOwner_whenNoItems() {
        mockOwnerExists();

        when(itemRepository.findAllByOwnerId(owner.getId()))
                .thenReturn(List.of());
        List<ItemWithBookingsDto> items = itemService.getItemsByOwner(owner.getId());

        assertEquals(0, items.size());
        verifyNoInteractions(bookingRepository, commentRepository);
    }

    @Test
    void getItemsByOwner_whenUserNoFound() {
        Long userId = 999L;
        when(entityFinder.getUserOrThrow(userId))
                .thenThrow(new NotFoundException("Пользователь не найден"));
        assertThrows(NotFoundException.class,
                () -> itemService.getItemsByOwner(userId));
        verifyNoInteractions(bookingRepository, commentRepository);
        verify(itemRepository, times(0)).findAllByOwnerId(userId);
    }

    @Test
    void searchItems_whenNoText() {
        String text = "";
        List<ItemDto> result = itemService.searchItems(text);
        assertEquals(0, result.size());
        verifyNoInteractions(itemRepository);
    }

    @Test
    void searchItems() {
        String text = "дрель";
        Item item = new Item();
        item.setId(10L);
        item.setName("Дрель");
        item.setDescription("Дрель для бетона");
        item.setAvailable(true);
        item.setOwner(owner);

        when(itemRepository.searchAvailableByText(text))
                .thenReturn(List.of(item));
        List<ItemDto> result = itemService.searchItems(text);

        assertEquals(1, result.size());
        assertEquals(item.getId(), result.get(0).getId());
        assertEquals(item.getName(), result.get(0).getName());
        assertEquals(item.isAvailable(), result.get(0).getAvailable());
        assertEquals(item.getDescription(), result.get(0).getDescription());

        verify(itemRepository, times(1)).searchAvailableByText(text);
    }

    private Item createItem(Long id, String name, String description) {
        Item item = new Item();
        item.setId(id);
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(true);
        item.setOwner(owner);
        return item;
    }

    private Booking createPastBooking(Long id, Item item, int daysAgo) {
        Booking booking = new Booking();
        booking.setId(id);
        booking.setItem(item);
        booking.setStatus(BookingStatus.APPROVED);
        booking.setEnd(LocalDateTime.now().minusDays(daysAgo));
        booking.setBooker(booker);
        return booking;
    }

    private Booking createFutureBooking(Long id, Item item, int daysAfter) {
        Booking booking = new Booking();
        booking.setId(id);
        booking.setItem(item);
        booking.setStatus(BookingStatus.APPROVED);
        booking.setStart(LocalDateTime.now().plusDays(daysAfter));
        booking.setBooker(booker);
        return booking;
    }

    private Comment createComment(Long id, Item item, String text) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setItem(item);
        comment.setText(text);
        comment.setAuthor(author);
        return comment;
    }

    private void mockOwnerExists() {
        when(entityFinder.getUserOrThrow(owner.getId()))
                .thenReturn(owner);
    }
}