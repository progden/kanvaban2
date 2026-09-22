package io.progden.kanban.core.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * {@code Item} 的持久化 port，由 kanban-spring 的 persistence 層實作。
 */
public interface ItemRepository {

    void save(Item item);

    void deleteById(UUID itemId);

    Optional<Item> findById(UUID itemId);

    List<Item> findByCanvasId(UUID canvasId);

    List<Item> findByCanvasIdAndAnchor(UUID canvasId, ItemAnchor anchor);
}
