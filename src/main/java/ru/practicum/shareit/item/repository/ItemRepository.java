package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository {

    Item save(Item item);

    Item update(Item item);

    Item findById(Long itemId);

    List<Item> findAllByOwnerId(Long ownerId);

    List<Item> searchAvailableByText(String text);
}
