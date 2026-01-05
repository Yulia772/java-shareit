package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final EntityFinder entityFinder;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        User existsUser = entityFinder.getUserOrThrow(userId);
        validateItemForCreate(itemDto);
        Item item = ItemMapper.toItem(itemDto, existsUser, null);

        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public  ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        entityFinder.getUserOrThrow(userId);
        Item foundItem = entityFinder.getItemOrThrow(itemId);
        if (!foundItem.getOwner().getId().equals(userId)) {
            log.warn("Пользователь userId={} не является владельцем этой вещи id={}", userId, foundItem.getOwner().getId());
            throw new NotFoundException("Пользователь не является владельцем этой вещи");
        }
        if (itemDto.getName() != null) {
            if (itemDto.getName().isBlank()) {
                log.warn("На обновление передано пустое имя");
                throw new ValidationException("Передано пустое имя");
            }
            foundItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            if (itemDto.getDescription().isBlank()) {
                log.warn("Передано пустое описание");
                throw new ValidationException("Передано пустое описание");
            }
            foundItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            foundItem.setAvailable(itemDto.getAvailable());
        }

        return ItemMapper.toItemDto(itemRepository.save(foundItem));
    }

    @Override
    public ItemWithBookingsDto getItem(Long userId, Long itemId) {
        Item item = entityFinder.getItemOrThrow(itemId);
        LocalDateTime now = LocalDateTime.now();
        Booking lastBooking = null;
        Booking nextBooking = null;
        if (Objects.equals(userId, item.getOwner().getId())) {
            lastBooking = bookingRepository.findFirstByItemIdAndEndBeforeAndStatusOrderByEndDesc(
                            itemId,
                            now,
                            BookingStatus.APPROVED
                    )
                    .orElse(null);
            nextBooking = bookingRepository.findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(
                            itemId,
                            now,
                            BookingStatus.APPROVED
                    )
                    .orElse(null);
        }

        List<Comment> comments = commentRepository.findByItemIdOrderByCreatedDesc(itemId);

        return ItemMapper.toItemWithBookingsDto(item, lastBooking, nextBooking, comments);
    }

    @Override
    public List<ItemWithBookingsDto> getItemsByOwner(Long userId) {
        entityFinder.getUserOrThrow(userId);

        List<Item> items = itemRepository.findAllByOwnerId(userId);
        if (items.isEmpty()) {
            return List.of();
        }
        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .toList();
        LocalDateTime now = LocalDateTime.now();

        List<Booking> pastBookings = bookingRepository
                .findByItemIdInAndEndBeforeAndStatusOrderByEndDesc(
                        itemIds,
                        now,
                        BookingStatus.APPROVED
                );

        Map<Long, Booking> lastBookingByItemId = new HashMap<>();
        for (Booking booking : pastBookings) {
            Long itemId = booking.getItem().getId();
            lastBookingByItemId.putIfAbsent(itemId, booking);
        }
        List<Booking> futureBookings = bookingRepository
                .findByItemIdInAndStartAfterAndStatusOrderByStartAsc(
                        itemIds,
                        now,
                        BookingStatus.APPROVED
                );
        Map<Long, Booking> nextBookingByItemId = new HashMap<>();
        for (Booking booking : futureBookings) {
            Long itemId = booking.getItem().getId();
            nextBookingByItemId.putIfAbsent(itemId, booking);
        }

        List<Comment> comments = commentRepository.findByItemIdInOrderByCreatedDesc(itemIds);
        Map<Long, List<Comment>> commentByItemId = comments.stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId()));

        return items.stream()
                .map(item -> {
                    Long itemId = item.getId();
                    Booking lastBooking = lastBookingByItemId.get(itemId);
                    Booking nextBooking = nextBookingByItemId.get(itemId);
                    List<Comment> itemComments = commentByItemId
                            .getOrDefault(itemId, List.of());

                    return ItemMapper.toItemWithBookingsDto(
                            item,
                            lastBooking,
                            nextBooking,
                            itemComments
                    );
                })
                .toList();
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.searchAvailableByText(text).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    private void validateItemForCreate(ItemDto itemDto) {
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            log.warn("Наименование вещи не может быть пустым");
            throw new ValidationException("Наименование вещи не может быть пустым");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            log.warn("Описание вещи не может быть пустым");
            throw new ValidationException("Описание вещи не может быть пустым");
        }
        if (itemDto.getAvailable() == null) {
            log.warn("Поле available (доступность вещи) должно быть указано при создании");
            throw new ValidationException("Не указана доступность вещи (available)");
        }
    }
}
