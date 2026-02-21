package ru.practicum.shareit.request.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.common.EntityFinder;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
@Transactional
public class ItemRequestServiceImpl implements ItemRequestService {
    private final EntityFinder entityFinder;
    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;

    @Override
    public ItemRequestDto createRequest(Long userId, NewItemRequestDto dto) {
        User user = entityFinder.getUserOrThrow(userId);
        ItemRequest itemRequest = ItemRequestMapper.mapToItemRequest(dto, user);
        itemRequest.setCreated(LocalDateTime.now());
        return ItemRequestMapper.mapToItemRequestDto(itemRequestRepository.save(itemRequest));
    }

    @Override
    @Transactional(readOnly = true)
    public ItemRequestWithItemsDto getRequest(Long userId, Long requestId) {
        entityFinder.getUserOrThrow(userId);
        ItemRequest request = entityFinder.getItemRequestOrThrow(requestId);
        List<Item> foundItems = itemRepository.findAllByRequest_Id(requestId);
        List<ItemShortDto> itemShortDtos = foundItems.stream()
                .map(ItemMapper::toItemShortDto)
                .toList();
        return ItemRequestMapper.mapToItemRequestWithItemsDto(request, itemShortDtos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestWithItemsDto> getUserRequests(Long userId) {
        entityFinder.getUserOrThrow(userId);
        //все запросы этого пользователя, сортировка уже в репозитории
        List<ItemRequest> requests = itemRequestRepository.findAllByRequester_IdOrderByCreatedDesc(userId);
        if (requests.isEmpty()) {
            return List.of();
        }
        //собираем id всех запросов
        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .toList();

        List<Item> items = itemRepository.findAllByRequest_IdIn(requestIds);

        return buildRequestDtos(requests, items);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestWithItemsDto> getOtherUserRequests(Long userId, int from, int size) {
        entityFinder.getUserOrThrow(userId);

        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "created"));

        List<ItemRequest> requests = itemRequestRepository.findAllByRequester_IdNot(userId, pageable);

        if (requests.isEmpty()) {
            return List.of();
        }

        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .toList();

        List<Item> items = itemRepository.findAllByRequest_IdIn(requestIds);

        return buildRequestDtos(requests, items);
    }

    private List<ItemRequestWithItemsDto> buildRequestDtos(List<ItemRequest> requests, List<Item> items) {
        Map<Long, List<ItemShortDto>> itemsByRequestId = items.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getRequest().getId(),
                        Collectors.mapping(ItemMapper::toItemShortDto, Collectors.toList())
                ));

        return requests.stream()
                .map(request -> {
                    List<ItemShortDto> requestItems = itemsByRequestId.getOrDefault(request.getId(), List.of());
                    return ItemRequestMapper.mapToItemRequestWithItemsDto(request, requestItems);
                })
                .toList();
    }
}
