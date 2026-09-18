package io.progden.kanban.spring.persistence;

import io.progden.kanban.core.domain.ActivityRecordSnapshot;
import io.progden.kanban.core.domain.Card;
import io.progden.kanban.core.domain.CardRepository;
import io.progden.kanban.core.domain.CommentSnapshot;
import io.progden.kanban.core.domain.StageTransitionSnapshot;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/**
 * {@link CardRepository} port 的 JPA 實作，負責 {@code Card}（domain，含內部 Comment／
 * StageTransition／ActivityRecord）與 {@code CardJpaEntity} 互轉。
 */
@Repository
class CardRepositoryAdapter implements CardRepository {

    private final CardJpaRepository jpaRepository;

    CardRepositoryAdapter(CardJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(Card card) {
        CardJpaEntity entity = jpaRepository.findById(card.getId())
                .orElseGet(() -> new CardJpaEntity(card.getId(), card.getBoardId()));
        entity.update(card.getTitle(), card.getDescription(), card.getDueDate(), card.getLabels(),
                card.getSwimlaneId(), card.getStageId(), card.isDeleted(), card.getAssigneeIds());
        entity.replaceComments(card.getComments());
        entity.replaceStageTransitions(card.getStageTransitions());
        entity.replaceActivityLog(card.getActivityLog());
        jpaRepository.save(entity);
    }

    @Override
    public Optional<Card> findById(UUID cardId) {
        return jpaRepository.findById(cardId).map(this::toDomain);
    }

    @Override
    public List<Card> findActiveBySwimlaneId(UUID swimlaneId) {
        return jpaRepository.findBySwimlaneIdAndDeletedFalse(swimlaneId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Card> findActiveByStageId(UUID stageId) {
        return jpaRepository.findByStageIdAndDeletedFalse(stageId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Card> findActiveByBoardId(UUID boardId) {
        return jpaRepository.findByBoardIdAndDeletedFalse(boardId).stream().map(this::toDomain).toList();
    }

    private Card toDomain(CardJpaEntity entity) {
        List<CommentSnapshot> comments = entity.getComments().stream()
                .map(c -> new CommentSnapshot(c.getId(), c.getAuthorId(), c.getContent(), c.getCreatedAt()))
                .toList();
        List<StageTransitionSnapshot> transitions = entity.getStageTransitions().stream()
                .map(t -> new StageTransitionSnapshot(
                        t.getId(), t.getOperatorId(), t.getFromStageId(), t.getToStageId(), t.getOccurredAt()))
                .toList();
        List<ActivityRecordSnapshot> activityLog = entity.getActivityLog().stream()
                .map(a -> new ActivityRecordSnapshot(a.getId(), a.getOperatorId(), a.getAction(), a.getOccurredAt()))
                .toList();
        return Card.reconstruct(entity.getId(), entity.getBoardId(), entity.getTitle(), entity.getDescription(),
                entity.getDueDate(), entity.getLabels(), entity.getSwimlaneId(), entity.getStageId(),
                entity.isDeleted(), entity.getAssigneeIds(), comments, transitions, activityLog);
    }
}
