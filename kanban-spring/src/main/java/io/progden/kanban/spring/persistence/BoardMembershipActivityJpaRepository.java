package io.progden.kanban.spring.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardMembershipActivityJpaRepository extends JpaRepository<BoardMembershipActivityJpaEntity, UUID> {

    List<BoardMembershipActivityJpaEntity> findByBoardId(UUID boardId);
}
