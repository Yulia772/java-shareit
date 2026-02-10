package ru.practicum.shareit.request.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemRequestMapper {
    public static ItemRequestWithItemsDto mapToItemRequestWithItemsDto(ItemRequest itemRequest, List<ItemShortDto> items) {

        return new ItemRequestWithItemsDto(
                itemRequest.getId(),
                itemRequest.getDescription(),
                itemRequest.getCreated(),
                items
        );
    }

    public static ItemRequestDto mapToItemRequestDto(ItemRequest itemRequest) {
        return new ItemRequestDto(
                itemRequest.getId(),
                itemRequest.getDescription(),
                itemRequest.getRequester().getId(),
                itemRequest.getCreated()
        );
    }

    public static ItemRequest mapToItemRequest(NewItemRequestDto newItemRequestDto, User requester) {
        ItemRequest itemRequest = new ItemRequest();

        itemRequest.setRequester(requester);
        itemRequest.setDescription(newItemRequestDto.getDescription());

        return itemRequest;
    }

}
