package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.constants.HeaderConstants;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
public class ItemRequestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    ItemRequestService itemRequestService;

    private NewItemRequestDto request;

    @BeforeEach
    void setUp() {
        request = new NewItemRequestDto();
        request.setDescription("desc");
    }

    @Test
    void createRequest_shouldReturn201AndBody() throws Exception {
        long userId = 1L;
        ItemRequestDto response = new ItemRequestDto();
        response.setDescription(request.getDescription());
        response.setId(10L);

        when(itemRequestService.createRequest(eq(userId), any(NewItemRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/requests")
                        .header(HeaderConstants.USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.description").value(request.getDescription()));

        verify(itemRequestService, times(1))
                .createRequest(eq(userId), any(NewItemRequestDto.class));
    }

    @Test
    void createRequest_whenUserNotFound() throws Exception {
        long userId = 1L;
        ItemRequestDto response = new ItemRequestDto();
        response.setDescription(request.getDescription());

        when(itemRequestService.createRequest(eq(userId), any(NewItemRequestDto.class)))
                .thenThrow(new NotFoundException("Пользователь не найден"));

        mockMvc.perform(post("/requests")
                        .header(HeaderConstants.USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(itemRequestService, times(1))
                .createRequest(eq(userId), any(NewItemRequestDto.class));
    }


    @Test
    void getRequest_shouldReturn200AndItems() throws Exception {
        Long userId = 1L;
        Long requestId = 10L;

        ItemRequestWithItemsDto response = new ItemRequestWithItemsDto();
        response.setId(requestId);
        response.setDescription("Description");

        when(itemRequestService.getRequest(eq(userId), eq(requestId)))
                .thenReturn(response);

        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header(HeaderConstants.USER_ID, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestId))
                .andExpect(jsonPath("$.description").value(response.getDescription()));

        verify(itemRequestService, times(1))
                .getRequest(eq(userId), eq(requestId));
    }

    @Test
    void getRequest_shouldReturn404_whenUserNotFound() throws Exception {
        Long userId = 1L;
        Long requestId = 10L;
        when(itemRequestService.getRequest(eq(userId), eq(requestId)))
                .thenThrow(new NotFoundException("Пользователь не найден"));

        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header(HeaderConstants.USER_ID, userId))
                .andExpect(status().isNotFound());

        verify(itemRequestService, times(1))
                .getRequest(eq(userId), eq(requestId));
    }

    @Test
    void getUserRequests_shouldReturn200AndList() throws Exception {
        long userId = 1L;

        ItemRequestWithItemsDto response1 = new ItemRequestWithItemsDto();
        response1.setId(userId);
        response1.setDescription("Description");

        ItemRequestWithItemsDto response2 = new ItemRequestWithItemsDto();
        response2.setId(userId);
        response2.setDescription("Description2");

        List<ItemRequestWithItemsDto> list = List.of(response1, response2);

        when(itemRequestService.getUserRequests(userId))
                .thenReturn(list);

        mockMvc.perform(get("/requests")
                        .header(HeaderConstants.USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(response1.getId()))
                .andExpect(jsonPath("$[0].description").value(response1.getDescription()))
                .andExpect(jsonPath("$[1].id").value(response2.getId()))
                .andExpect(jsonPath("$[1].description").value(response2.getDescription()));
        verify(itemRequestService, times(1))
                .getUserRequests(eq(userId));
    }

    @Test
    void getUserRequests_shouldReturn404_whenUserNotFound() throws Exception {
        Long userId = 1L;
        when(itemRequestService.getUserRequests(eq(userId)))
                .thenThrow(new NotFoundException("Пользователь не найден"));

        mockMvc.perform(get("/requests")
                        .header(HeaderConstants.USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(itemRequestService, times(1))
                .getUserRequests(eq(userId));
    }

    @Test
    void getOtherUserRequests_shouldReturn200AndList() throws Exception {
        Long userId = 10L;
        int from = 10;
        int size = 10;

        ItemRequestWithItemsDto response1 = new ItemRequestWithItemsDto();
        response1.setId(100L);
        response1.setDescription("Description");

        ItemRequestWithItemsDto response2 = new ItemRequestWithItemsDto();
        response2.setId(200L);
        response2.setDescription("Description2");

        List<ItemRequestWithItemsDto> response = List.of(response1, response2);

        when(itemRequestService.getOtherUserRequests(userId, from, size))
                .thenReturn(response);

        mockMvc.perform(get("/requests/all")
                        .header(HeaderConstants.USER_ID, userId)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
        verify(itemRequestService, times(1))
                .getOtherUserRequests(userId, from, size);
    }
}
