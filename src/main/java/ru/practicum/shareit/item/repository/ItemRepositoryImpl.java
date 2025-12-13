package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;

@Repository
public class ItemRepositoryImpl implements ItemRepository {

    private final Map<Long, Item> storage = new HashMap<>();
    private long countId = 0;

    @Override
    public Item save(Item item) {
        if(item.getId() == null) {
            countId++;
            item.setId(countId);
        }
        storage.put(item.getId(), item);
        return item;
    }

    @Override
    public Item update(Item item) {
        storage.put(item.getId(), item);
        return item;
    }

    @Override
    public Item findById(Long itemId) {
        return storage.get(itemId);
    }

    @Override
    public List<Item> findAllByOwnerId(Long ownerId) {
        return storage.values().stream()
                .filter(item -> item.getOwner() != null)
                .filter(item -> Objects.equals(item.getOwner().getId(), ownerId))
                .toList();
    }


    @Override
    public List<Item> searchAvailableByText(String text) {

        String query = text.trim().toLowerCase();
        return storage.values().stream()
                .filter(item -> item.isAvailable())
                .filter(item ->
                        (item.getName() != null && item.getName().toLowerCase().contains(query))
                                || (item.getDescription() != null && item.getDescription().toLowerCase().contains(query)))
                .toList();
    }
}
