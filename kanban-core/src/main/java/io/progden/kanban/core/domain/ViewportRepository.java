package io.progden.kanban.core.domain;

import java.util.Optional;
import java.util.UUID;

/**
 * {@code Viewport} 的持久化 port，由 kanban-spring 的 persistence 層實作。
 */
public interface ViewportRepository {

    void save(Viewport viewport);

    Optional<Viewport> findByCanvasIdAndUserId(UUID canvasId, UUID userId);
}
