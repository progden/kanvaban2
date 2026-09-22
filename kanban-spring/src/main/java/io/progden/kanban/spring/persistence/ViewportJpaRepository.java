package io.progden.kanban.spring.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ViewportJpaRepository extends JpaRepository<ViewportJpaEntity, UUID> {

    Optional<ViewportJpaEntity> findByCanvasIdAndUserId(UUID canvasId, UUID userId);
}
