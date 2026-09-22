package io.progden.kanban.spring.persistence;

import io.progden.kanban.core.domain.Viewport;
import io.progden.kanban.core.domain.ViewportRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/**
 * {@link ViewportRepository} port 的 JPA 實作，負責 {@code Viewport}（domain）與
 * {@code ViewportJpaEntity} 互轉。
 */
@Repository
class ViewportRepositoryAdapter implements ViewportRepository {

    private final ViewportJpaRepository jpaRepository;

    ViewportRepositoryAdapter(ViewportJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(Viewport viewport) {
        ViewportJpaEntity entity = jpaRepository.findById(viewport.getId())
                .orElseGet(() -> new ViewportJpaEntity(viewport.getId(), viewport.getCanvasId(),
                        viewport.getUserId()));
        entity.update(viewport.getX(), viewport.getY(), viewport.getZoom());
        jpaRepository.save(entity);
    }

    @Override
    public Optional<Viewport> findByCanvasIdAndUserId(UUID canvasId, UUID userId) {
        return jpaRepository.findByCanvasIdAndUserId(canvasId, userId).map(this::toDomain);
    }

    private Viewport toDomain(ViewportJpaEntity entity) {
        return Viewport.reconstruct(
                entity.getId(), entity.getCanvasId(), entity.getUserId(), entity.getX(), entity.getY(),
                entity.getZoom());
    }
}
