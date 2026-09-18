package io.progden.kanban.spring.persistence;

import io.progden.kanban.core.domain.BoardMembership;
import io.progden.kanban.core.domain.BoardMembershipRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/**
 * {@link BoardMembershipRepository} port 的 JPA 實作，負責 {@code BoardMembership}（domain）與
 * {@code BoardMembershipJpaEntity} 互轉。
 */
@Repository
class BoardMembershipRepositoryAdapter implements BoardMembershipRepository {

    private final BoardMembershipJpaRepository jpaRepository;

    BoardMembershipRepositoryAdapter(BoardMembershipJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(BoardMembership boardMembership) {
        BoardMembershipJpaEntity entity = jpaRepository.findById(boardMembership.getId())
                .orElseGet(() -> new BoardMembershipJpaEntity(boardMembership.getId(), boardMembership.getBoardId(),
                        boardMembership.getUserId(), boardMembership.getRole()));
        entity.updateRole(boardMembership.getRole());
        jpaRepository.save(entity);
    }

    @Override
    public void delete(BoardMembership boardMembership) {
        jpaRepository.deleteById(boardMembership.getId());
    }

    @Override
    public Optional<BoardMembership> findById(UUID membershipId) {
        return jpaRepository.findById(membershipId).map(this::toDomain);
    }

    @Override
    public Optional<BoardMembership> findByBoardIdAndUserId(UUID boardId, UUID userId) {
        return jpaRepository.findByBoardIdAndUserId(boardId, userId).map(this::toDomain);
    }

    @Override
    public List<BoardMembership> findByBoardId(UUID boardId) {
        return jpaRepository.findByBoardId(boardId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<BoardMembership> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).stream().map(this::toDomain).toList();
    }

    private BoardMembership toDomain(BoardMembershipJpaEntity entity) {
        return BoardMembership.reconstruct(entity.getId(), entity.getBoardId(), entity.getUserId(), entity.getRole());
    }
}
