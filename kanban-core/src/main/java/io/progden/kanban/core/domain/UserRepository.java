package io.progden.kanban.core.domain;

import java.util.Optional;
import java.util.UUID;

/**
 * {@code User} 的持久化 port，由 kanban-spring 的 persistence 層實作。
 */
public interface UserRepository {

    boolean existsByUsername(String username);

    void save(User user);

    Optional<User> findByUsername(String username);

    Optional<User> findById(UUID userId);
}
