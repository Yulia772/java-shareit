package ru.practicum.shareit.request;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.constants.HeaderConstants;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ResponseEntity<ItemRequestDto> createRequest(
            @RequestHeader(HeaderConstants.USER_ID) Long userId,
            @RequestBody NewItemRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(itemRequestService.createRequest(userId, dto));
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<ItemRequestWithItemsDto> getRequest(
            @RequestHeader(HeaderConstants.USER_ID) Long userId,
            @PathVariable Long requestId) {
        return ResponseEntity.ok()
                .body(itemRequestService.getRequest(userId, requestId));
    }

    @GetMapping
    public ResponseEntity<List<ItemRequestWithItemsDto>> getUserRequests(
            @RequestHeader(HeaderConstants.USER_ID) Long userId) {
        return ResponseEntity.ok()
                .body(itemRequestService.getUserRequests(userId));
    }


    @GetMapping("/all")
    public ResponseEntity<List<ItemRequestWithItemsDto>> getOtherUserRequests(
            @RequestHeader(HeaderConstants.USER_ID) Long userId,
            @RequestParam int from,
            @RequestParam int size) {
        return ResponseEntity.ok().body(itemRequestService.getOtherUserRequests(userId, from, size));
    }
}

