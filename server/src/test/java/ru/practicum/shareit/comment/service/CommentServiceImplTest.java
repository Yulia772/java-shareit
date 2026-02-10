package ru.practicum.shareit.comment.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.common.EntityFinder;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceImplTest {

    @Mock
    EntityFinder entityFinder;

    @InjectMocks
    CommentServiceImpl commentService;

    @Mock
    CommentRepository commentRepository;

    @Mock
    BookingService bookingService;

    @Test
    void addComment() {

        Long userId = 100L;
        User user = new User();
        user.setName("Author");

        Long itemId = 99L;
        Item item = new Item();

        CommentDto request = new CommentDto();
        request.setId(200L);
        request.setText("Wow");
        request.setAuthorName("Author");
        request.setCreated(LocalDateTime.of(2022, 12,1,22,15));

        Comment comment = new Comment();
        comment.setAuthor(user);
        comment.setText(request.getText());
        comment.setCreated(request.getCreated());
        comment.setItem(item);

        when(entityFinder.getUserOrThrow(userId))
                .thenReturn(user);

        when(entityFinder.getItemOrThrow(itemId))
                .thenReturn(item);

        when(commentRepository.save(any(Comment.class)))
                .thenReturn(comment);

        CommentDto saved = commentService.addComment(userId, itemId, request);
        assertEquals(saved.getText(), request.getText());
        assertEquals(saved.getCreated(), request.getCreated());
        assertEquals(saved.getAuthorName(), request.getAuthorName());
    }

    @Test
    void addComment_whenUserNotFound() {
        Long userId = 100L;
        Long itemId = 99L;
        CommentDto dto = new CommentDto();

        when(entityFinder.getUserOrThrow(userId))
                .thenThrow(new NotFoundException("Пользователь не найден"));

        assertThrows(NotFoundException.class,
                () -> commentService.addComment(userId, itemId, dto));
        verifyNoInteractions(commentRepository);
        verify(bookingService, never()).validateUserCanComment(eq(userId), eq(itemId));
    }

    @Test
    void addComment_whenItemNotFound() {
        Long userId = 100L;
        User user = new User();

        Long itemId = 99L;
        CommentDto dto = new CommentDto();

        when(entityFinder.getUserOrThrow(userId))
                .thenReturn(user);

        when(entityFinder.getItemOrThrow(itemId))
                .thenThrow(new NotFoundException("Вещь не найдена"));

        assertThrows(NotFoundException.class,
                () -> commentService.addComment(userId, itemId, dto));
        verifyNoInteractions(commentRepository);
        verify(bookingService, never()).validateUserCanComment(eq(userId), eq(itemId));
    }

    @Test
    void addComment_whenNoEndsBooking() {
        Long userId = 100L;
        User user = new User();

        Long itemId = 99L;
        Item item = new Item();

        CommentDto dto = new CommentDto();

        when(entityFinder.getUserOrThrow(userId))
                .thenReturn(user);

        when(entityFinder.getItemOrThrow(itemId))
                .thenReturn(item);

        doThrow(new ValidationException("Нет завершенного бронирования"))
                .when(bookingService).validateUserCanComment(userId, itemId);

        assertThrows(ValidationException.class,
                () -> commentService.addComment(userId, itemId, dto));
        verifyNoInteractions(commentRepository);
        verify(bookingService, times(1)).validateUserCanComment(userId, itemId);
    }

    @Test
    void addComment_whenNoCommentText() {
        Long userId = 100L;
        User user = new User();

        Long itemId = 99L;
        Item item = new Item();

        CommentDto dto = new CommentDto();
        dto.setText(" ");

        when(entityFinder.getUserOrThrow(userId))
                .thenReturn(user);

        when(entityFinder.getItemOrThrow(itemId))
                .thenReturn(item);

        assertThrows(ValidationException.class,
                () -> commentService.addComment(userId, itemId, dto));
        verify(bookingService, times(1)).validateUserCanComment(userId, itemId);
        verify(commentRepository, never()).save(any());
    }
}
