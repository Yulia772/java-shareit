package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.constants.HeaderConstants;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@RestController
@RequestMapping("/items")
@AllArgsConstructor
public class ItemController {
    private final ItemService itemService;

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
            @PathVariable("itemId") Long itemId,
            @RequestBody ItemDto itemDto) {
        return ResponseEntity.ok()
                .body(itemService.updateItem(userId,itemId,itemDto));
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDto> getItem(
            @RequestHeader(HeaderConstants.USER_ID) Long userId,
            @PathVariable Long itemId) {
        return ResponseEntity.ok().body(itemService.getItem(itemId));
    }


    @GetMapping
    public ResponseEntity<List<ItemDto>> getItemsByOwner(@RequestHeader(HeaderConstants.USER_ID) Long userId) {
        return ResponseEntity.ok().body(itemService.getItemsByOwner(userId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> searchItems(
            @RequestHeader(HeaderConstants.USER_ID) Long userId,
            @RequestParam("text") String text) {
        return ResponseEntity.ok().body(itemService.searchItems(text));
    }
}
