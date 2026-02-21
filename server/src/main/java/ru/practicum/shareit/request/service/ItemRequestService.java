package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;

import java.util.List;

public interface ItemRequestService {

        ItemRequestDto createRequest(Long userId, NewItemRequestDto dto);

        ItemRequestWithItemsDto getRequest(Long userId, Long requestId);

        List<ItemRequestWithItemsDto> getUserRequests(Long userId);

        List<ItemRequestWithItemsDto> getOtherUserRequests(Long userId, int from, int size);
}
