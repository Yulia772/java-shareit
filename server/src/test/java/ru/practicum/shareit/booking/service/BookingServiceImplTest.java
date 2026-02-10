package ru.practicum.shareit.booking.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingServiceImplTest {
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final EntityManager em;
    private record TestData(User owner, User booker, Item item, LocalDateTime now) {
    }

    @Test
    void createBooking_withEntityManager() {
        TestData data = baseData("em", true);

        LocalDateTime start = data.now().plusDays(1);
        LocalDateTime end = data.now().plusDays(2);

        BookingRequestDto dto = makeBookingRequestDto(
                data.item().getId(),
                start,
                end
        );

        BookingResponseDto saved = bookingService.createBooking(
                data.booker().getId(),
                dto
        );

        Booking found = em.createQuery(
                        "select b from Booking b " +
                                "where b.id = :id",
                        Booking.class
                )
                .setParameter("id", saved.getId())
                .getSingleResult();

        assertThat(found.getId(), notNullValue());
        assertThat(found.getStatus(), equalTo(BookingStatus.WAITING));
        assertThat(found.getStart(), equalTo(start));
        assertThat(found.getEnd(), equalTo(end));
        assertThat(found.getItem().getId(), equalTo(data.item().getId()));
        assertThat(found.getBooker().getId(), equalTo(data.booker().getId()));
    }

    @Test
    void createBooking() {
        TestData data = baseData("0", true);
        LocalDateTime start = data.now().plusDays(1);
        LocalDateTime end = data.now().plusDays(2);

        BookingRequestDto dto = makeBookingRequestDto(data.item().getId(), start, end);

        BookingResponseDto result = bookingService.createBooking(data.booker().getId(), dto);
        assertThat(result.getId(), notNullValue());
        assertThat(result.getStart(), equalTo(dto.getStart()));
        assertThat(result.getEnd(), equalTo(dto.getEnd()));
        assertThat(result.getStatus(), equalTo(BookingStatus.WAITING));
        assertThat(result.getBooker().getId(), equalTo(data.booker().getId()));
        assertThat(result.getItem().getId(), equalTo(data.item().getId()));

        Booking found = bookingRepository.findById(result.getId()).orElseThrow();
        assertThat(found.getStatus(), equalTo(BookingStatus.WAITING));
        assertThat(found.getBooker().getId(), equalTo(data.booker().getId()));
        assertThat(found.getItem().getId(), equalTo(data.item().getId()));
        assertThat(found.getStart(), equalTo(dto.getStart()));
        assertThat(found.getEnd(), equalTo(dto.getEnd()));
    }

    @Test
    void createBooking_whenOwnerBooksOwnItem_thenThrowValidationException() {
        User owner = saveUser("Owner", "owner1@test.com");
        Item item = saveItem(owner, true);
        LocalDateTime now = LocalDateTime.now();
        BookingRequestDto dto = makeBookingRequestDto(item.getId(), now.plusDays(1), now.plusDays(2));

        assertThrows(ValidationException.class,
                () -> bookingService.createBooking(owner.getId(), dto));
    }

    @Test
    void createBooking_whenItemUnavailable_thenThrowValidationException() {
        TestData data = baseData("2", false);
        LocalDateTime start = data.now().plusDays(1);
        LocalDateTime end = data.now().plusDays(2);
        BookingRequestDto dto = makeBookingRequestDto(data.item().getId(), start, end);

        assertThrows(ValidationException.class,
                () -> bookingService.createBooking(data.booker().getId(), dto));
    }

    @Test
    void createBooking_whenDateOfBookingInThePast_thenThrowValidationException() {
        TestData data = baseData("3", true);
        BookingRequestDto dto = makeBookingRequestDto(
                data.item().getId(),
                data.now().minusDays(2),
                data.now().minusDays(1)
        );

        assertThrows(ValidationException.class,
                () -> bookingService.createBooking(data.booker().getId(), dto));
    }

    @Test
    void createBooking_whenDateOfBookingAreEqual_thenThrowValidationException() {
        TestData data = baseData("4", true);
        LocalDateTime start = data.now().plusDays(1);
        BookingRequestDto dto = makeBookingRequestDto(data.item().getId(), start, start);

        assertThrows(ValidationException.class,
                () -> bookingService.createBooking(data.booker().getId(), dto));
    }

    @Test
    void createBooking_whenDateOfBookingIsNull_thenThrowValidationException() {
        TestData data = baseData("5", true);
        BookingRequestDto dto = makeBookingRequestDto(data.item().getId(), data.now().plusDays(1), null);

        assertThrows(ValidationException.class,
                () -> bookingService.createBooking(data.booker().getId(), dto));
    }

    @Test
    void updateBookingApproval() {
        TestData data = baseData("6", true);
        BookingResponseDto booking = createWaitingBooking(
                data,
                data.now().plusDays(1),
                data.now().plusDays(2)
        );

        BookingResponseDto approved = bookingService.updateBookingApproval(
                data.owner().getId(),
                booking.getId(),
                true
        );

        assertThat(approved.getStatus(), equalTo(BookingStatus.APPROVED));
        assertThat(approved.getId(), equalTo(booking.getId()));
        assertThat(approved.getBooker().getId(), equalTo(data.booker().getId()));
        assertThat(approved.getItem().getId(), equalTo(data.item().getId()));

        Booking found = bookingRepository.findById(approved.getId()).orElseThrow();
        assertThat(found.getStatus(), equalTo(BookingStatus.APPROVED));
    }

    @Test
    void updateBookingApproval_whenApprovedFalse_thenStatusRejected() {
        TestData data = baseData("7", true);
        BookingResponseDto booking = createWaitingBooking(
                data,
                data.now().plusDays(1),
                data.now().plusDays(2)
        );

        BookingResponseDto rejected = bookingService.updateBookingApproval(
                data.owner().getId(),
                booking.getId(),
                false
        );

        assertThat(rejected.getStatus(), equalTo(BookingStatus.REJECTED));

        Booking found = bookingRepository.findById(rejected.getId()).orElseThrow();
        assertThat(found.getStatus(), equalTo(BookingStatus.REJECTED));
    }

    @Test
    void updateBookingApproval_whenNotOwnerApproved_thenValidationException() {
        TestData data = baseData("8", true);
        BookingResponseDto booking = createWaitingBooking(
                data,
                data.now().plusDays(1),
                data.now().plusDays(2)
        );

        assertThrows(ValidationException.class,
                () -> bookingService.updateBookingApproval(
                        data.booker().getId(),
                        booking.getId(),
                        true
                ));
    }

    @Test
    void updateBookingApproval_whenApproveAlreadyApproved_thenValidationException() {
        TestData data = baseData("9", true);
        BookingResponseDto booking = createWaitingBooking(
                data,
                data.now().plusDays(1),
                data.now().plusDays(2)
        );

        BookingResponseDto approved = bookingService.updateBookingApproval(
                data.owner().getId(),
                booking.getId(),
                true
        );

        assertThrows(ValidationException.class,
                () -> bookingService.updateBookingApproval(
                        data.owner().getId(),
                        approved.getId(),
                        true
                ));
    }

    @Test
    void updateBookingApproval_whenApproveAlreadyRejected_thenValidationException() {
        TestData data = baseData("10", true);
        BookingResponseDto booking = createWaitingBooking(
                data,
                data.now().plusDays(1),
                data.now().plusDays(2)
        );

        BookingResponseDto rejected = bookingService.updateBookingApproval(
                data.owner().getId(),
                booking.getId(),
                false
        );

        assertThrows(ValidationException.class,
                () -> bookingService.updateBookingApproval(
                        data.owner().getId(),
                        rejected.getId(),
                        true
                ));
    }

    @Test
    void getBooking_asOwner() {
        TestData data = baseData("11", true);
        BookingResponseDto booking = createWaitingBooking(
                data,
                data.now().plusDays(1),
                data.now().plusDays(2)
        );
        BookingResponseDto getBooking = bookingService.getBooking(
                data.owner().getId(),
                booking.getId()
        );
        assertThat(getBooking.getId(), equalTo(booking.getId()));
        assertThat(getBooking.getStatus(), equalTo(booking.getStatus()));
        assertThat(getBooking.getBooker().getId(), equalTo(data.booker().getId()));
        assertThat(getBooking.getItem().getId(), equalTo(data.item().getId()));
        assertThat(getBooking.getStart(), equalTo(booking.getStart()));
        assertThat(getBooking.getEnd(), equalTo(booking.getEnd()));
    }

    @Test
    void getBooking_asBooker() {
        TestData data = baseData("12", true);
        BookingResponseDto booking = createWaitingBooking(
                data,
                data.now().plusDays(1),
                data.now().plusDays(2)
        );
        BookingResponseDto getBooking = bookingService.getBooking(
                data.booker().getId(),
                booking.getId()
        );
        assertThat(getBooking.getId(), equalTo(booking.getId()));
        assertThat(getBooking.getStatus(), equalTo(booking.getStatus()));
        assertThat(getBooking.getBooker().getId(), equalTo(data.booker().getId()));
        assertThat(getBooking.getItem().getId(), equalTo(data.item().getId()));
        assertThat(getBooking.getStart(), equalTo(booking.getStart()));
        assertThat(getBooking.getEnd(), equalTo(booking.getEnd()));
    }

    @Test
    void getBooking_asOther_thenNotFoundException() {
        TestData data = baseData("13", true);
        User other = saveUser("Other", "other13@test.com");
        BookingResponseDto booking = createWaitingBooking(
                data,
                data.now().plusDays(1),
                data.now().plusDays(2)
        );
        assertThrows(NotFoundException.class,
                () -> bookingService.getBooking(
                        other.getId(),
                        booking.getId()
                        ));
    }

    @Test
    void getBookingByBooker_whenStateAll() {
        TestData data = baseData("14", true);
        BookingResponseDto booking = createWaitingBooking(
                data,
                data.now().plusDays(1),
                data.now().plusDays(2)
        );
        List<BookingResponseDto> bookings = bookingService.getBookingByBooker(
                data.booker().getId(),
                BookingState.ALL,
                0, 10
        );
        assertThat(bookings.size(), equalTo(1));
        assertThat(bookings.get(0).getId(), equalTo(booking.getId()));
        assertThat(bookings.get(0).getBooker().getId(), equalTo(data.booker().getId()));
    }

    @Test
    void getBookingByBooker_whenStatusWaiting() {
        TestData data = baseData("15", true);
        BookingResponseDto booking = createWaitingBooking(
                data,
                data.now().plusDays(1),
                data.now().plusDays(2)
        );
        List<BookingResponseDto> bookings = bookingService.getBookingByBooker(
                data.booker().getId(),
                BookingState.WAITING,
                0, 10
        );
        assertThat(bookings.size(), equalTo(1));
        assertThat(bookings.get(0).getId(), equalTo(booking.getId()));
        assertThat(bookings.get(0).getStatus(), equalTo(BookingStatus.WAITING));
    }

    @Test
    void getBookingByBooker_whenStatusRejected() {
        TestData data = baseData("16", true);
        BookingResponseDto booking = createWaitingBooking(
                data,
                data.now().plusDays(1),
                data.now().plusDays(2)
        );
        bookingService.updateBookingApproval(data.owner().getId(), booking.getId(), false);
        List<BookingResponseDto> bookings = bookingService.getBookingByBooker(
                data.booker().getId(),
                BookingState.REJECTED,
                0, 10
        );
        assertThat(bookings.size(), equalTo(1));
        assertThat(bookings.get(0).getId(), equalTo(booking.getId()));
        assertThat(bookings.get(0).getStatus(), equalTo(BookingStatus.REJECTED));
    }

    @Test
    void getBookingByBooker_whenWrongPagination() {
        TestData data = baseData("17", true);
        assertThrows(ValidationException.class,
                () -> bookingService.getBookingByBooker(
                        data.booker().getId(),
                        BookingState.WAITING,
                        -10, 10
                ));
    }

    @Test
    void getBookingByOwner_whenStateAll() {
        TestData data = baseData("18", true);
        BookingResponseDto booking = createWaitingBooking(
                data,
                data.now().plusDays(1),
                data.now().plusDays(2)
        );
        List<BookingResponseDto> bookings = bookingService.getBookingByOwner(
                data.owner().getId(),
                BookingState.ALL,
                0, 10
        );
        assertThat(bookings.size(), equalTo(1));
        assertThat(bookings.get(0).getId(), equalTo(booking.getId()));
        assertThat(bookings.get(0).getBooker().getId(), equalTo(data.booker().getId()));
    }

    @Test
    void getBookingByOwner_whenStatusWaiting() {
        TestData data = baseData("19", true);
        BookingResponseDto booking = createWaitingBooking(
                data,
                data.now().plusDays(1),
                data.now().plusDays(2)
        );
        List<BookingResponseDto> bookings = bookingService.getBookingByOwner(
                data.owner().getId(),
                BookingState.WAITING,
                0, 10
        );
        assertThat(bookings.size(), equalTo(1));
        assertThat(bookings.get(0).getId(), equalTo(booking.getId()));
        assertThat(bookings.get(0).getStatus(), equalTo(BookingStatus.WAITING));
    }

    @Test
    void getBookingByOwner_whenStatusRejected() {
        TestData data = baseData("20", true);
        BookingResponseDto booking = createWaitingBooking(
                data,
                data.now().plusDays(1),
                data.now().plusDays(2)
        );
        bookingService.updateBookingApproval(data.owner().getId(), booking.getId(), false);
        List<BookingResponseDto> bookings = bookingService.getBookingByOwner(
                data.owner().getId(),
                BookingState.REJECTED,
                0, 10
        );
        assertThat(bookings.size(), equalTo(1));
        assertThat(bookings.get(0).getId(), equalTo(booking.getId()));
        assertThat(bookings.get(0).getStatus(), equalTo(BookingStatus.REJECTED));
    }

    @Test
    void getBookingByOwner_whenWrongPagination() {
        TestData data = baseData("21", true);
        assertThrows(ValidationException.class,
                () -> bookingService.getBookingByOwner(
                        data.owner().getId(),
                        BookingState.WAITING,
                        0, 0
                ));
    }

    @Test
    void validateUserCanComment_whenNotHaveEndingBooking() {
        TestData data = baseData("22", true);
        assertThrows(ValidationException.class,
                () -> bookingService.validateUserCanComment(data.booker().getId(), data.item().getId()));
    }

    @Test
    void validateUserCanComment_whenEndingBookingExists() {
        TestData data = baseData("23", true);

        LocalDateTime start = data.now().minusDays(2);
        LocalDateTime end = data.now().minusDays(1);

        Booking booking = new Booking();
        booking.setItem(data.item());
        booking.setBooker(data.booker());
        booking.setStart(start);
        booking.setEnd(end);
        booking.setStatus(BookingStatus.APPROVED);
        Booking result = bookingRepository.save(booking);

        assertDoesNotThrow(() ->
                bookingService.validateUserCanComment(
                        result.getBooker().getId(),
                        result.getItem().getId()
                ));
    }

    // Helpers

    private TestData baseData(String suffix, boolean available) {
        User owner = saveUser("Owner", "owner" + suffix + "@test.com");
        User booker = saveUser("Booker", "booker" + suffix + "@test.com");
        Item item = saveItem(owner, available);
        LocalDateTime now = LocalDateTime.now();
        return new TestData(owner, booker, item, now);
    }

    private BookingResponseDto createWaitingBooking(
            TestData data,
            LocalDateTime start,
            LocalDateTime end
    ) {
        BookingRequestDto dto = makeBookingRequestDto(
                data.item().getId(),
                start,
                end
        );

        return bookingService.createBooking(
                data.booker().getId(),
                dto
        );
    }

    private User saveUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return userRepository.save(user);
    }

    private Item saveItem(User owner, boolean available) {
        Item item = new Item();
        item.setName("Item");
        item.setDescription("Desc");
        item.setAvailable(available);
        item.setOwner(owner);
        item.setRequest(null);
        return itemRepository.save(item);
    }

    private BookingRequestDto makeBookingRequestDto(Long itemId, LocalDateTime start, LocalDateTime end) {
        BookingRequestDto dto = new BookingRequestDto();
        dto.setItemId(itemId);
        dto.setStart(start);
        dto.setEnd(end);
        return dto;
    }
}