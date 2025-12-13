package ru.practicum.shareit.user.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final Map<Long, User> storage = new HashMap<>();
    private long countId = 0;

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            countId++;
            user.setId(countId);
        }
        storage.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        storage.put(user.getId(), user);
        return user;
    }

    @Override
    public User findById(Long id) {
        return storage.get(id);
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public void deleteById(Long id) {
        storage.remove(id);
    }

    @Override
    public  boolean existsByEmail(String email) {
        Collection<User> users = storage.values();
        for (User u : users) {
            if (Objects.equals(u.getEmail(), email)) {
                return true;
            }
        }
        return false;
    }
}
