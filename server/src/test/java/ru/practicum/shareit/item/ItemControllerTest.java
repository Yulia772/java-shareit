package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.service.CommentService;
import ru.practicum.shareit.constants.HeaderConstants;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
public class ItemControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    ItemService itemService;

    @MockBean
    CommentService commentService;

    private ItemDto request;

    @BeforeEach
    void setUp() {
        request = new ItemDto();
        request.setName("Drıll");
        request.setDescription("Powerful drıll");
        request.setAvailable(true);
    }

    @Test
    void createItem_shouldReturn201AndBody() throws Exception {
        Long itemId = 1L;
        Long userId = 10L;

        ItemDto response = new ItemDto();
        response.setId(itemId);
        response.setName(request.getName());
        response.setDescription(request.getDescription());
        response.setAvailable(request.getAvailable());
        when(itemService.createItem(eq(userId), any(ItemDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/items")
                        .header(HeaderConstants.USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value(request.getName()))
                .andExpect(jsonPath("$.description").value(request.getDescription()))
                .andExpect(jsonPath("$.available").value(request.getAvailable()));
        verify(itemService, times(1))
                .createItem(eq(userId), any(ItemDto.class));
    }

    @Test
    void createItem_whenUserNotFound_shouldReturn404() throws Exception {
        Long userId = 10L;
        when(itemService.createItem(eq(userId), any(ItemDto.class)))
                .thenThrow(new NotFoundException("Пользователь не найден"));

        mockMvc.perform(post("/items")
                        .header(HeaderConstants.USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(itemService, times(1))
                .createItem(eq(userId), any(ItemDto.class));
    }

    @Test
    void createItem_whenValidationException_shouldReturn400() throws Exception {
        Long userId = 10L;

        when(itemService.createItem(eq(userId), any(ItemDto.class)))
                .thenThrow(new ValidationException("Невалидные данные"));

        mockMvc.perform(post("/items")
                        .header(HeaderConstants.USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(itemService, times(1))
                .createItem(eq(userId), any(ItemDto.class));
    }

    @Test
    void updateItem_shouldReturn200AndBody() throws Exception {
        Long itemId = 1L;
        Long userId = 10L;

        ItemDto response = new ItemDto();
        response.setId(itemId);
        response.setName(request.getName());
        response.setDescription(request.getDescription());
        response.setAvailable(request.getAvailable());

        when(itemService.updateItem(eq(userId), eq(itemId), Mockito.any(ItemDto.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/items/{id}", itemId)
                        .header(HeaderConstants.USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value(request.getName()))
                .andExpect(jsonPath("$.description").value(request.getDescription()))
                .andExpect(jsonPath("$.available").value(request.getAvailable()));
        verify(itemService, times(1))
                .updateItem(eq(userId), eq(itemId), any(ItemDto.class));
    }

    @Test
    void updateItem_shouldReturn404_whenUserNotOwner() throws Exception {
        Long userId = 1L;
        Long itemId = 10L;
        when(itemService.updateItem(eq(userId), eq(itemId), any(ItemDto.class)))
                .thenThrow(new NotFoundException("Пользователь не владелец вещи"));

        mockMvc.perform(patch("/items/{id}", itemId)
                        .header(HeaderConstants.USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(itemService, times(1))
                .updateItem(eq(userId), eq(itemId), any(ItemDto.class));
    }

    @Test
    void getItem_shouldReturn200AndBody() throws Exception {
        Long userId = 1L;
        Long itemId = 10L;

        BookingShortDto lastBooking = new BookingShortDto();
        lastBooking.setId(42L);
        lastBooking.setBookerId(7L);

        CommentDto comment = new CommentDto();
        comment.setId(99L);
        comment.setText("super");

        CommentDto comment1 = new CommentDto();
        comment1.setId(88L);
        comment1.setText("Wow");

        List<CommentDto> comments = List.of(comment, comment1);

        ItemWithBookingsDto response = new ItemWithBookingsDto();
        response.setId(itemId);
        response.setName(request.getName());
        response.setDescription(request.getDescription());
        response.setAvailable(request.getAvailable());
        response.setLastBooking(lastBooking);
        response.setComments(comments);

        when(itemService.getItem(eq(userId), eq(itemId)))
                .thenReturn(response);

        mockMvc.perform(get("/items/{id}", itemId)
                        .header(HeaderConstants.USER_ID, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value(request.getName()))
                .andExpect(jsonPath("$.description").value(request.getDescription()))
                .andExpect(jsonPath("$.available").value(request.getAvailable()))
                .andExpect(jsonPath("$.lastBooking").exists())
                .andExpect(jsonPath("$.lastBooking.id").value(lastBooking.getId()))
                .andExpect(jsonPath("$.lastBooking.bookerId").value(lastBooking.getBookerId()))
                .andExpect(jsonPath("$.comments", hasSize(2)))
                .andExpect(jsonPath("$.comments").isArray())
                .andExpect(jsonPath("$.comments[0].id").value(comment.getId()))
                .andExpect(jsonPath("$.comments[0].text").value(comment.getText()))
                .andExpect(jsonPath("$.comments[1].id").value(comment1.getId()))
                .andExpect(jsonPath("$.comments[1].text").value(comment1.getText()));
        verify(itemService, times(1))
                .getItem(eq(userId), eq(itemId));
    }

    @Test
    void getItem_whenNotOwner_shouldReturn200AndBody() throws Exception {
        Long itemId = 10L;
        Long user2Id = 2L;

        CommentDto comment = new CommentDto();
        comment.setId(99L);
        comment.setText("super");

        CommentDto comment1 = new CommentDto();
        comment1.setId(88L);
        comment1.setText("Wow");

        List<CommentDto> comments = List.of(comment, comment1);

        ItemWithBookingsDto response = new ItemWithBookingsDto();
        response.setId(itemId);
        response.setName(request.getName());
        response.setDescription(request.getDescription());
        response.setAvailable(request.getAvailable());
        response.setLastBooking(null);
        response.setComments(comments);

        when(itemService.getItem(eq(user2Id), eq(itemId)))
                .thenReturn(response);

        mockMvc.perform(get("/items/{id}", itemId)
                        .header(HeaderConstants.USER_ID, user2Id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value(request.getName()))
                .andExpect(jsonPath("$.description").value(request.getDescription()))
                .andExpect(jsonPath("$.available").value(request.getAvailable()))
                .andExpect(jsonPath("$.lastBooking").value(nullValue()))
                .andExpect(jsonPath("$.comments", hasSize(2)))
                .andExpect(jsonPath("$.comments").isArray())
                .andExpect(jsonPath("$.comments[0].id").value(comment.getId()))
                .andExpect(jsonPath("$.comments[0].text").value(comment.getText()))
                .andExpect(jsonPath("$.comments[1].id").value(comment1.getId()))
                .andExpect(jsonPath("$.comments[1].text").value(comment1.getText()));
        verify(itemService, times(1))
                .getItem(eq(user2Id), eq(itemId));
    }

    @Test
    void getItem_shouldReturn404_whenItemNotFound() throws Exception {
        Long itemId = 1L;
        Long userId = 2L;
        when(itemService.getItem(eq(userId), eq(itemId)))
                .thenThrow(new NotFoundException("Пользователь не найден"));

        mockMvc.perform(get("/items/{id}", itemId)
                .header(HeaderConstants.USER_ID, userId))
                .andExpect(status().isNotFound());

        verify(itemService, times(1))
                .getItem(eq(userId), eq(itemId));
    }

    @Test
    void getItemsByOwner_shouldReturn200AndList() throws Exception {
        Long ownerId = 1L;

        BookingShortDto lastBooking = new BookingShortDto();
        lastBooking.setId(42L);
        lastBooking.setBookerId(7L);

        BookingShortDto nextBooking = new BookingShortDto();
        nextBooking.setId(40L);
        nextBooking.setBookerId(5L);

        ItemWithBookingsDto dto = new ItemWithBookingsDto();
        dto.setId(100L);
        dto.setName("Item1");
        dto.setAvailable(true);
        dto.setComments(List.of());
        dto.setLastBooking(lastBooking);

        ItemWithBookingsDto dto1 = new ItemWithBookingsDto();
        dto1.setId(200L);
        dto1.setName("Item1");
        dto1.setAvailable(false);
        dto1.setComments(List.of());
        dto1.setNextBooking(nextBooking);

        List<ItemWithBookingsDto> response = List.of(dto, dto1);

        when(itemService.getItemsByOwner(ownerId))
                .thenReturn(response);

        mockMvc.perform(get("/items")
                        .header(HeaderConstants.USER_ID, ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(dto.getId()))
                .andExpect(jsonPath("$[0].name").value(dto.getName()))
                .andExpect(jsonPath("$[0].available").value(dto.getAvailable()))
                .andExpect(jsonPath("$[0].lastBooking.id").value(lastBooking.getId()))
                .andExpect(jsonPath("$[0].lastBooking.bookerId").value(lastBooking.getBookerId()))
                .andExpect(jsonPath("$[1].id").value(dto1.getId()))
                .andExpect(jsonPath("$[1].name").value(dto1.getName()))
                .andExpect(jsonPath("$[1].available").value(dto1.getAvailable()))
                .andExpect(jsonPath("$[1].nextBooking.id").value(nextBooking.getId()))
                .andExpect(jsonPath("$[1].nextBooking.bookerId").value(nextBooking.getBookerId()));
        verify(itemService, times(1))
                .getItemsByOwner(ownerId);
    }

    @Test
    void getItemsByOwner_whenOwnerNotFound_shouldReturn404() throws Exception {
        Long ownerId = 1L;
        when(itemService.getItemsByOwner(eq(ownerId)))
                .thenThrow(new NotFoundException("Пользователь не найден"));
        mockMvc.perform(get("/items")
                .header(HeaderConstants.USER_ID, ownerId))
                .andExpect(status().isNotFound());
        verify(itemService, times(1))
                .getItemsByOwner(ownerId);
    }

    @Test
    void searchItems_ok() throws Exception {
        Long userId = 1L;
        String text = "дрель";

        ItemDto dto = new ItemDto();
        dto.setId(100L);
        dto.setName("Дрель");
        dto.setDescription("Powerful");
        dto.setAvailable(true);

        List<ItemDto> response = List.of(dto);

        when(itemService.searchItems(text))
                .thenReturn(response);

        mockMvc.perform(get("/items/search")
                        .header(HeaderConstants.USER_ID, userId)
                .param("text", text))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(dto.getId()))
                .andExpect(jsonPath("$[0].name").value(dto.getName()))
                .andExpect(jsonPath("$[0].description").value(dto.getDescription()))
                .andExpect(jsonPath("$[0].available").value(dto.getAvailable()));

        verify(itemService, times(1)).searchItems(text);
    }

    @Test
    void searchItems_whenTextIsNull() throws Exception {
        Long userId = 1L;
        String text = " ";

        when(itemService.searchItems(text))
                .thenReturn(List.of());

        mockMvc.perform(get("/items/search")
                        .header(HeaderConstants.USER_ID, userId)
                        .param("text", text))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void createComment_ok() throws Exception {
        Long userId = 100L;
        Long itemId = 200L;

        CommentDto comment = new CommentDto();
        comment.setId(98L);
        comment.setText("Wow");
        comment.setAuthorName("Jack");
        comment.setCreated(LocalDateTime.of(2022, 12, 10, 5, 9));

        when(commentService.addComment(eq(userId), eq(itemId), any(CommentDto.class)))
                .thenReturn(comment);

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(HeaderConstants.USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(comment)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(comment.getId()))
                .andExpect(jsonPath("$.text").value(comment.getText()))
                .andExpect(jsonPath("$.authorName").value(comment.getAuthorName()))
                .andExpect(jsonPath("$.created").exists());

        verify(commentService, times(1)).addComment(eq(userId), eq(itemId), any(CommentDto.class));
    }

    @Test
    void createComment_whenCommentIsEmpty() throws Exception {
        Long userId = 100L;
        Long itemId = 200L;

        CommentDto comment = new CommentDto();

        when(commentService.addComment(eq(userId), eq(itemId), any(CommentDto.class)))
                .thenThrow(new ValidationException("Комментарий пустой"));

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(HeaderConstants.USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(comment)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createComment_whenUserNotFound() throws Exception {
        Long userId = 100L;
        Long itemId = 200L;

        CommentDto comment = new CommentDto();
        comment.setText("text");

        when(commentService.addComment(eq(userId), eq(itemId), any(CommentDto.class)))
                .thenThrow(new NotFoundException("Пользователь не найден"));

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(HeaderConstants.USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(comment)))
                .andExpect(status().isNotFound());
    }
}





