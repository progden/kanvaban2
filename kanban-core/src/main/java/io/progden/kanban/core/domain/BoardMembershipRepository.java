package io.progden.kanban.core.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * {@code BoardMembership} 的持久化 port，由 kanban-spring 的 persistence 層實作。
 */
public interface BoardMembershipRepository {

    void save(BoardMembership boardMembership);

    void delete(BoardMembership boardMembership);

    Optional<BoardMembership> findById(UUID membershipId);

    Optional<BoardMembership> findByBoardIdAndUserId(UUID boardId, UUID userId);

    List<BoardMembership> findByBoardId(UUID boardId);

    List<BoardMembership> findByUserId(UUID userId);
}
