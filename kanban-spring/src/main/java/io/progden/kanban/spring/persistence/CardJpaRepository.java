package io.progden.kanban.spring.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardJpaRepository extends JpaRepository<CardJpaEntity, UUID> {

    List<CardJpaEntity> findByBoardIdAndDeletedFalse(UUID boardId);

    List<CardJpaEntity> findBySwimlaneIdAndDeletedFalse(UUID swimlaneId);

    List<CardJpaEntity> findByStageIdAndDeletedFalse(UUID stageId);
}
