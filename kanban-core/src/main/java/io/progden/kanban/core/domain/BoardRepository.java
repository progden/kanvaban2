package io.progden.kanban.core.domain;

import java.util.Optional;
import java.util.UUID;

/**
 * {@code Board} 的持久化 port，由 kanban-spring 的 persistence 層實作。
 */
public interface BoardRepository {

    void save(Board board);

    Optional<Board> findById(UUID boardId);
}
