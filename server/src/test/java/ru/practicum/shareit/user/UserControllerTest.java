package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    UserService userService;

    private UserDto request;

    @BeforeEach
    void setUp() {
        request = new UserDto();
        request.setName("User");
        request.setEmail("user@mail.ru");
    }

    @Test
    void createUser_shouldReturn201AndBody() throws Exception {
        long userId = 1L;
        UserDto response = new UserDto();
        response.setId(userId);
        response.setName(request.getName());
        response.setEmail(request.getEmail());
        when(userService.createUser(Mockito.any(UserDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.email").value(request.getEmail()))
                .andExpect(jsonPath("$.name").value(request.getName()));
        verify(userService, times(1))
                .createUser(Mockito.any(UserDto.class));
    }

    @Test
    void updateUser_shouldReturn200AndBody() throws Exception {
        long userId = 1L;

        UserDto response = new UserDto();
        response.setId(userId);
        response.setName(request.getName());
        response.setEmail(request.getEmail());
        when(userService.updateUser(eq(userId), any(UserDto.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.email").value(request.getEmail()))
                .andExpect(jsonPath("$.name").value(request.getName()));
        verify(userService, times(1))
                .updateUser(eq(userId), any(UserDto.class));
    }

    @Test
    void updateUser_shouldReturn404_whenUserNotFound() throws Exception {
        long userId = 1L;
        when(userService.updateUser(eq(userId), any(UserDto.class)))
                .thenThrow(new NotFoundException("Пользователь не найден"));

        mockMvc.perform(patch("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(userService, times(1))
                .updateUser(eq(userId), any(UserDto.class));
    }

    @Test
    void getUser_shouldReturn200AndBody() throws Exception {
        long userId = 1L;

        UserDto response = new UserDto();
        response.setId(userId);
        response.setName(request.getName());
        response.setEmail(request.getEmail());

        Mockito
                .when(userService.getUser(userId))
                .thenReturn(response);

        mockMvc.perform(get("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.email").value(request.getEmail()))
                .andExpect(jsonPath("$.name").value(request.getName()));
        verify(userService, times(1))
                .getUser(eq(userId));
    }

    @Test
    void getUser_shouldReturn404_whenUserNotFound() throws Exception {
        long userId = 1L;
        when(userService.getUser(eq(userId)))
                .thenThrow(new NotFoundException("Пользователь не найден"));

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isNotFound());

        verify(userService, times(1))
                .getUser(eq(userId));
    }

    @Test
    void getAllUsers_shouldReturn200AndList() throws Exception {
        List<UserDto> response = List.of();

        Mockito
                .when(userService.getAllUsers())
                .thenReturn(response);

        mockMvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        verify(userService, times(1))
                .getAllUsers();
    }

    @Test
    void deleteUser_shouldReturn204() throws Exception {
        long userId = 1L;
        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isOk());
        verify(userService, times(1)).deleteUser(userId);
    }

    @Test
    void deleteUser_whenUserNotFound() throws Exception {
        long userId = 1L;
        doThrow(new NotFoundException("Пользователь не найден"))
                .when(userService).deleteUser(userId);
        mockMvc.perform(delete("/users/{id}", userId))
                        .andExpect(status().isNotFound());

        verify(userService, times(1)).deleteUser(userId);
    }
}
