package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.constants.HeaderConstants;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    BookingService bookingService;

    @Test
    void createBooking_shouldReturn201AndBody() throws Exception {
        long bookerId = 1L;

        BookingRequestDto request = new BookingRequestDto();
        request.setItemId(10L);
        request.setStart(LocalDateTime.of(2026, 2, 1, 10, 0));
        request.setEnd(LocalDateTime.of(2026, 2, 2, 10, 0));

        BookingResponseDto response = new BookingResponseDto();
        response.setId(100L);
        response.setStart(request.getStart());
        response.setEnd(request.getEnd());
        response.setStatus(BookingStatus.WAITING);
        Mockito
                .when(bookingService.createBooking(Mockito.eq(bookerId), Mockito.any(BookingRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/bookings")
                        .header(HeaderConstants.USER_ID, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.status").value("WAITING"));
        verify(bookingService, times(1))
                .createBooking(Mockito.eq(bookerId),Mockito.any(BookingRequestDto.class));
    }

    @Test
    void updateBooking_shouldReturn200AndBody() throws Exception {
        long ownerId = 1L;
        long bookingId = 10L;
        boolean approved = true;

        BookingResponseDto response = new BookingResponseDto();
        response.setId(100L);
        response.setStatus(BookingStatus.APPROVED);

        Mockito
                .when(bookingService.updateBookingApproval(ownerId, bookingId, approved))
                .thenReturn(response);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(HeaderConstants.USER_ID, ownerId)
                        .param("approved", String.valueOf(approved)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.status").value("APPROVED"));
        verify(bookingService, times(1))
                .updateBookingApproval(ownerId, bookingId, approved);
    }

    @Test
    void updateBooking_shouldReturn400_withoutApprovalStatus() throws Exception {
        long ownerId = 1L;
        long bookingId = 10L;

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                         .header(HeaderConstants.USER_ID, ownerId))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void getBooking_shouldReturn200AndBody() throws Exception {
        long ownerId = 1L;
        long bookingId = 10L;

        BookingResponseDto response = new BookingResponseDto();
        response.setId(100L);
        response.setStatus(BookingStatus.WAITING);

        Mockito
                .when(bookingService.getBooking(ownerId, bookingId))
                .thenReturn(response);

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header(HeaderConstants.USER_ID, ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.status").value("WAITING"));
        verify(bookingService, times(1))
                .getBooking(ownerId, bookingId);
    }

    @Test
    void getBooking_shouldReturn400_ifNoHeader() throws Exception {
        long bookingId = 10L;

        mockMvc.perform(get("/bookings/{bookingId}", bookingId))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void getBookingsForBooker() throws Exception {
        long userId = 1L;
        int from = 0;
        int size = 10;

        List<BookingResponseDto> response = List.of();

        Mockito
                .when(bookingService.getBookingByBooker(userId, BookingState.ALL, from, size))
                .thenReturn(response);

        mockMvc.perform(get("/bookings")
                        .header(HeaderConstants.USER_ID, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        verify(bookingService, times(1))
                .getBookingByBooker(userId, BookingState.ALL, from, size);
    }

    @Test
    void getBookingsForBooker_stateLowerCase() throws Exception {
        long userId = 1L;
        int from = 5;
        int size = 2;

        List<BookingResponseDto> response = List.of();

        Mockito
                .when(bookingService.getBookingByBooker(userId, BookingState.WAITING, from, size))
                .thenReturn(response);

        mockMvc.perform(get("/bookings")
                        .header(HeaderConstants.USER_ID, userId)
                        .param("state", "waiting")
                        .param("from", "5")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        verify(bookingService, times(1))
                .getBookingByBooker(userId, BookingState.WAITING, from, size);
    }

    @Test
    void getBookingsForBooker_stateUnknown() throws Exception {
        long userId = 1L;

        mockMvc.perform(get("/bookings")
                        .header(HeaderConstants.USER_ID, userId)
                        .param("state", "unknown")
                        .param("from", "5")
                        .param("size", "2"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(bookingService);
    }

    @Test
    void getBookingsForOwner() throws Exception {
        long userId = 1L;
        int from = 0;
        int size = 10;

        List<BookingResponseDto> response = List.of();

        Mockito
                .when(bookingService.getBookingByOwner(userId, BookingState.ALL, from, size))
                .thenReturn(response);

        mockMvc.perform(get("/bookings/owner")
                        .header(HeaderConstants.USER_ID, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        verify(bookingService, times(1))
                .getBookingByOwner(userId, BookingState.ALL, from, size);
    }

    @Test
    void getBookingsForOwner_stateLowerCase() throws Exception {
        long userId = 1L;
        int from = 5;
        int size = 2;

        List<BookingResponseDto> response = List.of();

        Mockito
                .when(bookingService.getBookingByOwner(userId, BookingState.WAITING, from, size))
                .thenReturn(response);

        mockMvc.perform(get("/bookings/owner")
                        .header(HeaderConstants.USER_ID, userId)
                        .param("state", "waiting")
                        .param("from", "5")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        verify(bookingService, times(1))
                .getBookingByOwner(userId, BookingState.WAITING, from, size);
    }

    @Test
    void getBookingsForOwner_stateUnknown() throws Exception {
        long userId = 1L;

        mockMvc.perform(get("/bookings/owner")
                        .header(HeaderConstants.USER_ID, userId)
                        .param("state", "unknown")
                        .param("from", "5")
                        .param("size", "2"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(bookingService);
    }
}
