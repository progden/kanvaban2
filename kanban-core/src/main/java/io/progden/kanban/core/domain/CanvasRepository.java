package io.progden.kanban.core.domain;

import java.util.Optional;
import java.util.UUID;

/**
 * {@code Canvas} 的持久化 port，由 kanban-spring 的 persistence 層實作。
 */
public interface CanvasRepository {

    void save(Canvas canvas);

    Optional<Canvas> findById(UUID canvasId);

    Optional<Canvas> findByBoardId(UUID boardId);
}
