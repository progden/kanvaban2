package io.progden.kanban.spring.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardMembershipJpaRepository extends JpaRepository<BoardMembershipJpaEntity, UUID> {

    Optional<BoardMembershipJpaEntity> findByBoardIdAndUserId(UUID boardId, UUID userId);

    List<BoardMembershipJpaEntity> findByBoardId(UUID boardId);

    List<BoardMembershipJpaEntity> findByUserId(UUID userId);
}
