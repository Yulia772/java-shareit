package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;

import java.util.List;

public interface ItemService {
    ItemDto createItem(Long ownerId, ItemDto itemDto);

    ItemDto updateItem(Long ownerId, Long itemId, ItemDto itemDto);

    ItemWithBookingsDto getItem(Long userId, Long itemId);

    List<ItemWithBookingsDto> getItemsByOwner(Long ownerId);

    List<ItemDto> searchItems(String text);
}
