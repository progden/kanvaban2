package io.progden.kanban.spring.persistence;

import io.progden.kanban.core.domain.Item;
import io.progden.kanban.core.domain.ItemAnchor;
import io.progden.kanban.core.domain.ItemRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/**
 * {@link ItemRepository} port 的 JPA 實作，負責 {@code Item}（domain）與 {@code ItemJpaEntity} 互轉。
 */
@Repository
class ItemRepositoryAdapter implements ItemRepository {

    private final ItemJpaRepository jpaRepository;

    ItemRepositoryAdapter(ItemJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(Item item) {
        ItemJpaEntity entity = jpaRepository.findById(item.getId())
                .orElseGet(() -> new ItemJpaEntity(item.getId(), item.getCanvasId()));
        entity.update(item.getComponent(), item.getAnchor(), item.getX(), item.getY(), item.getWidth(),
                item.getHeight(), item.getZ(), item.isMovable(), item.isResizable(), item.isRemovable());
        jpaRepository.save(entity);
    }

    @Override
    public void deleteById(UUID itemId) {
        jpaRepository.deleteById(itemId);
    }

    @Override
    public Optional<Item> findById(UUID itemId) {
        return jpaRepository.findById(itemId).map(this::toDomain);
    }

    @Override
    public List<Item> findByCanvasId(UUID canvasId) {
        return jpaRepository.findByCanvasId(canvasId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Item> findByCanvasIdAndAnchor(UUID canvasId, ItemAnchor anchor) {
        return jpaRepository.findByCanvasIdAndAnchor(canvasId, anchor).stream().map(this::toDomain).toList();
    }

    private Item toDomain(ItemJpaEntity entity) {
        return Item.reconstruct(entity.getId(), entity.getCanvasId(), entity.getComponent(), entity.getAnchor(),
                entity.getX(), entity.getY(), entity.getWidth(), entity.getHeight(), entity.getZ(),
                entity.isMovable(), entity.isResizable(), entity.isRemovable());
    }
}
