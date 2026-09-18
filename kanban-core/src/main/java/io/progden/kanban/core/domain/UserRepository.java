package io.progden.kanban.core.domain;

import java.util.Optional;

/**
 * {@code User} 的持久化 port，由 kanban-spring 的 persistence 層實作。
 */
public interface UserRepository {

    boolean existsByUsername(String username);

    void save(User user);

    Optional<User> findByUsername(String username);
}
