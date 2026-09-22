package io.progden.kanban.spring.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CanvasJpaRepository extends JpaRepository<CanvasJpaEntity, UUID> {

    Optional<CanvasJpaEntity> findByBoardId(UUID boardId);
}
