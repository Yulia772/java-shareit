package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.service.CommentService;
import ru.practicum.shareit.constants.HeaderConstants;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@RestController
@RequestMapping("/items")
@AllArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<ItemDto> createItem(
            @RequestHeader(HeaderConstants.USER_ID) Long userId,
            @RequestBody ItemDto itemDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(itemService.createItem(userId, itemDto));
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDto> updateItem(
            @RequestHeader(HeaderConstants.USER_ID) Long userId,
            @PathVariable Long itemId,
            @RequestBody ItemDto itemDto) {
        return ResponseEntity.ok()
                .body(itemService.updateItem(userId,itemId,itemDto));
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemWithBookingsDto> getItem(
            @RequestHeader(HeaderConstants.USER_ID) Long userId,
            @PathVariable Long itemId) {
        return ResponseEntity.ok().body(itemService.getItem(userId, itemId));
    }


    @GetMapping
    public ResponseEntity<List<ItemWithBookingsDto>> getItemsByOwner(@RequestHeader(HeaderConstants.USER_ID) Long userId) {
        return ResponseEntity.ok().body(itemService.getItemsByOwner(userId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> searchItems(
            @RequestHeader(HeaderConstants.USER_ID) Long userId,
            @RequestParam("text") String text) {
        return ResponseEntity.ok().body(itemService.searchItems(text));
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<CommentDto> createComment(
            @RequestHeader(HeaderConstants.USER_ID) Long userId,
            @PathVariable Long itemId,
            @RequestBody CommentDto commentDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.addComment(userId, itemId, commentDto));
    }

}
