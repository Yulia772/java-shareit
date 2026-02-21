package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.common.EntityFinder;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceImplTest {

    @InjectMocks
    ItemRequestServiceImpl itemRequestService;

    @Mock
    EntityFinder entityFinder;

    @Mock
    ItemRequestRepository itemRequestRepository;

    @Mock
    ItemRepository itemRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("User");
        user.setEmail("user@mail.com");
    }

    @Test
    void createRequest_ok() {
        Long userId = 99L;

        NewItemRequestDto newDto = new NewItemRequestDto();
        newDto.setDescription("Description");

        ItemRequest savedEntity = makeItemRequest(10L, user, newDto.getDescription(), LocalDateTime.now());

        when(entityFinder.getUserOrThrow(userId))
                .thenReturn(user);

        when(itemRequestRepository.save(any(ItemRequest.class)))
                .thenReturn(savedEntity);

        ItemRequestDto result = itemRequestService.createRequest(userId, newDto);

        assertNotNull(result);
        assertEquals(savedEntity.getId(), result.getId());
        assertEquals(newDto.getDescription(), result.getDescription());
        assertNotNull(result.getCreated());

        verify(entityFinder, times(1)).getUserOrThrow(userId);
        verify(itemRequestRepository, times(1)).save(any(ItemRequest.class));
        verifyNoInteractions(itemRepository);
    }

    @Test
    void createRequest_whenUserNotFound_shouldThrow() {
        Long userId = 1L;

        NewItemRequestDto dto = new NewItemRequestDto();
        dto.setDescription("desc");
        when(entityFinder.getUserOrThrow(userId))
                .thenThrow(new NotFoundException("Пользователь не найден"));
        assertThrows(NotFoundException.class,
                () -> itemRequestService.createRequest(userId, dto));
        verify(entityFinder, times(1)).getUserOrThrow(userId);
        verify(itemRequestRepository, never()).save(any());
        verifyNoInteractions(itemRepository);
    }

    @Test
    void getRequest_ok() {
        Long userId = 1L;

        Long requestId = 100L;
        ItemRequest request = makeItemRequest(100L, user, "Need drill", LocalDateTime.now());

        Item item1 = makeItem(11L, request,"Drill", user);

        Item item2 = makeItem(12L, request, "Mixer", user);

        when(entityFinder.getUserOrThrow(userId))
                .thenReturn(user);
        when(entityFinder.getItemRequestOrThrow(requestId))
                .thenReturn(request);
        when(itemRepository.findAllByRequest_Id(requestId))
                .thenReturn(List.of(item1, item2));

        ItemRequestWithItemsDto result = itemRequestService.getRequest(userId, requestId);

        assertNotNull(result);
        assertEquals(requestId, result.getId());
        assertEquals(request.getDescription(),result.getDescription());
        assertNotNull(result.getCreated());

        assertNotNull(result.getItems());
        assertEquals(2, result.getItems().size());
        assertEquals(item1.getId(), result.getItems().get(0).getId());
        assertEquals(item1.getName(), result.getItems().get(0).getName());
        assertEquals(user.getId(), result.getItems().get(0).getOwnerId());

        assertEquals(item2.getId(), result.getItems().get(1).getId());
        assertEquals(item2.getName(), result.getItems().get(1).getName());
        assertEquals(user.getId(), result.getItems().get(1).getOwnerId());

        verify(entityFinder).getUserOrThrow(userId);
        verify(entityFinder).getItemRequestOrThrow(requestId);
        verify(itemRepository).findAllByRequest_Id(requestId);
        verifyNoMoreInteractions(entityFinder, itemRepository);
        verifyNoInteractions(itemRequestRepository);
    }

    @Test
    void getRequest_whenRequestNotFound_shouldThrow() {
        Long userId = 100L;
        Long requestId = 200L;

        when(entityFinder.getUserOrThrow(userId))
                .thenReturn(user);
        when(entityFinder.getItemRequestOrThrow(requestId))
                .thenThrow(new NotFoundException("Запрос не найден"));

        assertThrows(NotFoundException.class,
                () -> itemRequestService.getRequest(userId, requestId));

        verify(entityFinder).getUserOrThrow(userId);
        verify(entityFinder).getItemRequestOrThrow(requestId);
        verify(itemRepository, never()).findAllByRequest_Id(anyLong());
        verifyNoInteractions(itemRequestRepository);
    }

    @Test
    void getUserRequests() {
        Long userId = 10L;
        user.setId(userId);

        ItemRequest r1 = makeItemRequest(100L, user, "r1", LocalDateTime.now());
        ItemRequest r2 = makeItemRequest(200L, user, "r2", LocalDateTime.now());

        Item i1 = makeItem(1L, r1, "Item-1", user);
        Item i2 = makeItem(2L, r2, "Item-2", user);
        Item i3 = makeItem(3L, r2, "Item-3", user);

        when(entityFinder.getUserOrThrow(userId))
                .thenReturn(user);
        when(itemRequestRepository.findAllByRequester_IdOrderByCreatedDesc(userId))
                .thenReturn(List.of(r1, r2));
        when(itemRepository.findAllByRequest_IdIn(List.of(r1.getId(), r2.getId())))
                .thenReturn(List.of(i1, i2, i3));

        List<ItemRequestWithItemsDto> result = itemRequestService.getUserRequests(userId);

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(r1.getId(), result.get(0).getId());
        assertEquals(1, result.get(0).getItems().size());
        assertEquals(i1.getId(), result.get(0).getItems().get(0).getId());

        assertEquals(r2.getId(), result.get(1).getId());
        assertEquals(2, result.get(1).getItems().size());

        verify(entityFinder).getUserOrThrow(userId);
        verify(itemRequestRepository).findAllByRequester_IdOrderByCreatedDesc(userId);
        verify(itemRepository).findAllByRequest_IdIn(List.of(r1.getId(), r2.getId()));
    }

    @Test
    void getUserRequests_whenNoRequests_shouldReturnEmpty() {
        Long userId = 1L;

        when(entityFinder.getUserOrThrow(userId))
                .thenReturn(user);
        when(itemRequestRepository.findAllByRequester_IdOrderByCreatedDesc(userId))
                .thenReturn(List.of());

        List<ItemRequestWithItemsDto> result = itemRequestService.getUserRequests(userId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(entityFinder).getUserOrThrow(userId);
        verify(itemRequestRepository).findAllByRequester_IdOrderByCreatedDesc(userId);
        verifyNoInteractions(itemRepository);
    }

    @Test
    void getOtherUserRequests_ok() {
        Long userId = 1L;
        user.setId(userId);

        int from = 10;
        int size = 5;

        when(entityFinder.getUserOrThrow(userId))
                .thenReturn(user);

        ItemRequest r1 = new ItemRequest();
        r1.setId(100L);

        ItemRequest r2 = new ItemRequest();
        r2.setId(200L);

        List<ItemRequest> requests = List.of(r1, r2);

        when(itemRequestRepository.findAllByRequester_IdNot(eq(userId), any(Pageable.class)))
                .thenReturn(requests);

        Item item1 = makeItem(1L, r1, "Item1", user);
        Item item2 = makeItem(2L, r2, "Item2", user);

        when(itemRepository.findAllByRequest_IdIn(eq(List.of(r1.getId(), r2.getId()))))
                .thenReturn(List.of(item1, item2));

        var result = itemRequestService.getOtherUserRequests(userId, from, size);

        assertEquals(2, result.size());
        assertEquals(r1.getId(), result.get(0).getId());
        assertEquals(r2.getId(), result.get(1).getId());

        assertNotNull(result.get(0).getItems());
        assertNotNull(result.get(1).getItems());

        verify(entityFinder, times(1)).getUserOrThrow(userId);
        verify(itemRequestRepository, times(1))
                .findAllByRequester_IdNot(eq(userId), any(Pageable.class));
        verify(itemRepository, times(1))
                .findAllByRequest_IdIn(eq(List.of(r1.getId(), r2.getId())));
    }

    @Test
    void getOtherUserRequests_whenNoRequests_matchAnyPageable() {
        Long userId = 1L;
        int from = 0;
        int size = 10;

        when(entityFinder.getUserOrThrow(userId))
                .thenReturn(user);

        when(itemRequestRepository.findAllByRequester_IdNot(eq(userId), any(Pageable.class)))
                .thenReturn(List.of());

        var result = itemRequestService.getOtherUserRequests(userId, from, size);

        assertTrue(result.isEmpty());

        verify(entityFinder, times(1)).getUserOrThrow(userId);
        verify(itemRequestRepository, times(1))
                .findAllByRequester_IdNot(eq(userId), any(Pageable.class));
        verifyNoInteractions(itemRepository);
    }

    @Test
    void getOtherUserRequests_whenUserNotFound() {
        Long userId = 1L;
        when(entityFinder.getUserOrThrow(userId))
                .thenThrow(new NotFoundException("Пользователь не найден"));

        assertThrows(NotFoundException.class,
                () -> itemRequestService.getOtherUserRequests(userId, 10, 15));
        verify(entityFinder).getUserOrThrow(userId);
        verifyNoInteractions(itemRequestRepository, itemRepository);
    }

    private Item makeItem(Long id, ItemRequest request, String name, User owner) {
        Item item = new Item();
        item.setId(id);
        item.setRequest(request);
        item.setName(name);
        item.setOwner(owner);
        return item;
    }

    private ItemRequest makeItemRequest(Long id, User requester, String description, LocalDateTime time) {
        ItemRequest request = new ItemRequest();
        request.setId(id);
        request.setRequester(requester);
        request.setDescription(description);
        request.setCreated(time);
        return request;
    }
}
