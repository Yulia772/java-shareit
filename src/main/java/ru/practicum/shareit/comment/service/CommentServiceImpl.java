package ru.practicum.shareit.comment.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.mapper.CommentMapper;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.common.EntityFinder;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

@Slf4j
@Service
@AllArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final EntityFinder entityFinder;
    private final BookingService bookingService;
    private final CommentRepository commentRepository;

    @Override
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        User user = entityFinder.getUserOrThrow(userId);
        Item item = entityFinder.getItemOrThrow(itemId);

        bookingService.validateUserCanComment(userId, itemId);

        validateComment(commentDto.getText());

        Comment saved = commentRepository.save(CommentMapper.toComment(commentDto, item, user));

        return CommentMapper.toCommentDto(saved);
    }

    private void validateComment(String text) {
        if (text == null || text.isBlank()) {
            log.warn("Текст комментария не может быть пустым");
            throw new ValidationException("Текст комментария не может быть пустым");
        }
    }
}
