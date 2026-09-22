package io.progden.kanban.spring.persistence;

import io.progden.kanban.core.domain.ItemAnchor;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemJpaRepository extends JpaRepository<ItemJpaEntity, UUID> {

    List<ItemJpaEntity> findByCanvasId(UUID canvasId);

    List<ItemJpaEntity> findByCanvasIdAndAnchor(UUID canvasId, ItemAnchor anchor);
}
