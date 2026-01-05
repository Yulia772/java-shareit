package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.comment.mapper.CommentMapper;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public class ItemMapper {

    public static ItemDto toItemDto(Item item) {
        if (item == null) {
            return null;
        }
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.isAvailable(),
                item.getRequest() != null ? item.getRequest().getId() : null
        );
    }

    public static Item toItem(ItemDto dto, User owner, ItemRequest request) {

         Item item = new Item();
         item.setName(dto.getName());
         item.setDescription(dto.getDescription());
         item.setAvailable(dto.getAvailable());
         item.setOwner(owner);
         item.setRequest(request);
         return item;
    }

    public static ItemWithBookingsDto toItemWithBookingsDto(
            Item item,
            Booking lastBooking,
            Booking nextBooking,
            List<Comment> comments
    ) {
        if (item == null) {
            return null;
        }

        ItemWithBookingsDto dto = new ItemWithBookingsDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.isAvailable());
        dto.setRequestId(item.getRequest() != null ? item.getRequest().getId() : null);

        dto.setLastBooking(lastBooking != null
                ? BookingMapper.toBookingShortDto(lastBooking)
                : null);

        dto.setNextBooking(nextBooking != null
                ? BookingMapper.toBookingShortDto(nextBooking)
                : null);

        dto.setComments(comments == null
                ? List.of()
                : comments.stream()
                .map(CommentMapper::toCommentDto)
                .toList());

        return dto;
    }
}
