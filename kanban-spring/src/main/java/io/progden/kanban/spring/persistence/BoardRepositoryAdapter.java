package io.progden.kanban.spring.persistence;

import io.progden.kanban.core.domain.ActivityRecordSnapshot;
import io.progden.kanban.core.domain.Board;
import io.progden.kanban.core.domain.BoardRepository;
import io.progden.kanban.core.domain.CardLookupPort;
import io.progden.kanban.core.domain.StageSnapshot;
import io.progden.kanban.core.domain.SwimlaneSnapshot;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/**
 * {@link BoardRepository} port 的 JPA 實作，負責 {@code Board}（domain，含內部 Swimlane／Stage／
 * ActivityRecord）與 {@code BoardJpaEntity} 互轉。
 */
@Repository
class BoardRepositoryAdapter implements BoardRepository {

    private final BoardJpaRepository jpaRepository;
    private final CardLookupPort cardLookupPort;

    BoardRepositoryAdapter(BoardJpaRepository jpaRepository, CardLookupPort cardLookupPort) {
        this.jpaRepository = jpaRepository;
        this.cardLookupPort = cardLookupPort;
    }

    @Override
    public void save(Board board) {
        BoardJpaEntity entity = jpaRepository.findById(board.getId())
                .orElseGet(() -> new BoardJpaEntity(board.getId(), board.getName(), board.getCreatedBy()));
        entity.updateName(board.getName());
        entity.replaceSwimlanes(board.getSwimlanes());
        entity.replaceStages(board.getStages());
        entity.replaceActivityLog(board.getActivityLog());
        jpaRepository.save(entity);
    }

    @Override
    public Optional<Board> findById(UUID boardId) {
        return jpaRepository.findById(boardId).map(this::toDomain);
    }

    @Override
    public void deleteById(UUID boardId) {
        jpaRepository.deleteById(boardId);
    }

    private Board toDomain(BoardJpaEntity entity) {
        List<SwimlaneSnapshot> swimlanes = entity.getSwimlanes().stream()
                .map(s -> new SwimlaneSnapshot(s.getId(), s.getName(), s.getSortOrder()))
                .toList();
        List<StageSnapshot> stages = entity.getStages().stream()
                .map(s -> new StageSnapshot(s.getId(), s.getName(), s.getSortOrder(), s.getRole()))
                .toList();
        List<ActivityRecordSnapshot> activityLog = entity.getActivityLog().stream()
                .map(a -> new ActivityRecordSnapshot(a.getId(), a.getOperatorId(), a.getAction(), a.getOccurredAt()))
                .toList();
        return Board.reconstruct(entity.getId(), entity.getName(), entity.getCreatedBy(),
                swimlanes, stages, activityLog, cardLookupPort);
    }
}
