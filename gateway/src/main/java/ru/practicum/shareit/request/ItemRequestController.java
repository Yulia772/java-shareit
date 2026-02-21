package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.constants.HeaderConstants;
import ru.practicum.shareit.request.dto.NewItemRequestDto;

@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> createRequest(@RequestHeader(HeaderConstants.USER_ID) long userId,
                                           @RequestBody @Valid NewItemRequestDto dto) {
        log.info("Создаем запрос на вещь {}, userId={}", dto, userId);
        return itemRequestClient.createRequest(userId, dto);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequest(@RequestHeader(HeaderConstants.USER_ID) long userId,
                                             @PathVariable Long requestId) {
        log.info("Получаем запрос id={}, userId={}", requestId, userId);
        return itemRequestClient.getRequest(userId, requestId);
    }

    @GetMapping
    public ResponseEntity<Object> getUserRequests(
            @RequestHeader(HeaderConstants.USER_ID) long userId) {
        log.info("Получаем запросы пользователя userId={}", userId);
        return itemRequestClient.getUserRequests(userId);
    }


    @GetMapping("/all")
    public ResponseEntity<Object> getOtherUserRequests(@RequestHeader(HeaderConstants.USER_ID) long userId,
                                              @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                              @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Получаем запросы других пользователей, userId={}, from={}, size={}", userId, from, size);
        return itemRequestClient.getOtherUserRequests(userId, from, size);
    }
}
