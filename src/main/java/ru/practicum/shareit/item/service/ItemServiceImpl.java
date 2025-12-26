package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        User existsUser = getUserOrThrow(userId);
        validateItemForCreate(itemDto);
        Item item = ItemMapper.toItem(itemDto, existsUser, null);

        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public  ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        getUserOrThrow(userId);
        Item foundItem = getItemOrThrow(itemId);
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

        return ItemMapper.toItemDto(itemRepository.update(foundItem));
    }

    @Override
    public ItemDto getItem(Long itemId) {
        Item item = getItemOrThrow(itemId);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getItemsByOwner(Long userId) {
        getUserOrThrow(userId);
        return itemRepository.findAllByOwnerId(userId).stream()
                .map(ItemMapper::toItemDto)
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

    private User getUserOrThrow(Long userId) {
        User existsUser = userRepository.findById(userId);
        if (existsUser == null) {
            log.warn("Пользователя с таким id={} не существует", userId);
            throw new NotFoundException("Пользователя с таким id не существует");
        }
        return existsUser;
    }

    private Item getItemOrThrow(Long itemId) {
        Item item = itemRepository.findById(itemId);
        if (item == null) {
            log.warn("Вещь с id={} не найдена", itemId);
            throw new NotFoundException("Вещь не найдена");
        }
        return item;
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
    }
}
