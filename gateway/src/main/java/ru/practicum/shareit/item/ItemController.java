package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.constants.HeaderConstants;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

@Controller
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestHeader(HeaderConstants.USER_ID) long userId,
                                             @RequestBody @Valid ItemDto requestDto) {
        log.info("Создание вещи {}, userId={}", requestDto, userId);
        return itemClient.createItem(userId, requestDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(
            @RequestHeader(HeaderConstants.USER_ID) long userId,
            @PathVariable long itemId,
            @RequestBody ItemDto requestDto) {
        log.info("Обновление вещи id={}, userId={}", itemId, userId);
        return itemClient.updateItem(userId,itemId,requestDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItem(
            @RequestHeader(HeaderConstants.USER_ID) long userId,
            @PathVariable long itemId) {
        log.info("Получение вещи id={}, userId={}", itemId, userId);
        return itemClient.getItem(userId, itemId);
    }


    @GetMapping
    public ResponseEntity<Object> getItemsByOwner(@RequestHeader(HeaderConstants.USER_ID) long userId) {
        log.info("Поиск вещей по владельцу userId={}", userId);
        return itemClient.getItemsByOwner(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(
            @RequestHeader(HeaderConstants.USER_ID) long userId,
            @RequestParam("text") String text) {
        log.info("Поиск вещей по тексту '{}', userId={}", text, userId);
        return itemClient.searchItems(userId, text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(
            @RequestHeader(HeaderConstants.USER_ID) long userId,
            @PathVariable long itemId,
            @RequestBody @Valid CommentDto commentDto) {
        log.info("Создание комментария к вещи id={}, userId={}", itemId, userId);
        return itemClient.createComment(userId, itemId, commentDto);
    }
}
