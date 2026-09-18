package io.progden.kanban.spring.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.progden.kanban.core.domain.ActivityRecord;
import io.progden.kanban.core.domain.Comment;
import io.progden.kanban.core.domain.StageTransition;

/**
 * {@code Card} 的 JPA 對應表格，只在 persistence 層使用，不外流到 domain／application 層。
 *
 * <p>Comment／StageTransition／ActivityRecord 皆以 {@code card} 的子集合儲存
 * （cascade + orphanRemoval），儲存時採「就地調和」（見 {@link #replaceComments}／
 * {@link #replaceStageTransitions}／{@link #replaceActivityLog}），比照 {@code BoardJpaEntity}。
 *
 * <p>只以 {@code boardId}／{@code swimlaneId}／{@code stageId}（UUID 欄位）參照所屬的
 * Board/Swimlane/Stage，不建立 JPA 關聯——{@code Card} 是獨立 Aggregate（design-kanban-basic.md 第 2 節）。
 */
@Entity
@Table(name = "cards")
public class CardJpaEntity {

    @Id
    private UUID id;

    @Column(name = "board_id", nullable = false)
    private UUID boardId;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "card_labels", joinColumns = @JoinColumn(name = "card_id"))
    @Column(name = "label")
    private List<String> labels = new ArrayList<>();

    @Column(name = "swimlane_id", nullable = false)
    private UUID swimlaneId;

    @Column(name = "stage_id", nullable = false)
    private UUID stageId;

    @Column(nullable = false)
    private boolean deleted;

    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("createdAt ASC")
    private List<CommentJpaEntity> comments = new ArrayList<>();

    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("occurredAt ASC")
    private List<StageTransitionJpaEntity> stageTransitions = new ArrayList<>();

    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<CardActivityRecordJpaEntity> activityLog = new ArrayList<>();

    protected CardJpaEntity() {
    }

    public CardJpaEntity(UUID id, UUID boardId) {
        this.id = id;
        this.boardId = boardId;
    }

    public void update(String title, String description, LocalDate dueDate, List<String> labels,
            UUID swimlaneId, UUID stageId, boolean deleted) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.labels = new ArrayList<>(labels);
        this.swimlaneId = swimlaneId;
        this.stageId = stageId;
        this.deleted = deleted;
    }

    public void replaceComments(List<Comment> source) {
        for (Comment comment : source) {
            if (findComment(comment.getId()) == null) {
                comments.add(new CommentJpaEntity(
                        comment.getId(), this, comment.getAuthorId(), comment.getContent(), comment.getCreatedAt()));
            }
        }
    }

    public void replaceStageTransitions(List<StageTransition> source) {
        for (StageTransition transition : source) {
            if (findStageTransition(transition.getId()) == null) {
                stageTransitions.add(new StageTransitionJpaEntity(transition.getId(), this,
                        transition.getOperatorId(), transition.getFromStageId(), transition.getToStageId(),
                        transition.getOccurredAt()));
            }
        }
    }

    public void replaceActivityLog(List<ActivityRecord> source) {
        for (ActivityRecord record : source) {
            if (findActivity(record.getId()) == null) {
                activityLog.add(new CardActivityRecordJpaEntity(
                        record.getId(), this, record.getOperatorId(), record.getAction(), record.getOccurredAt()));
            }
        }
    }

    private CommentJpaEntity findComment(UUID id) {
        return comments.stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
    }

    private StageTransitionJpaEntity findStageTransition(UUID id) {
        return stageTransitions.stream().filter(t -> t.getId().equals(id)).findFirst().orElse(null);
    }

    private CardActivityRecordJpaEntity findActivity(UUID id) {
        return activityLog.stream().filter(a -> a.getId().equals(id)).findFirst().orElse(null);
    }

    public UUID getId() {
        return id;
    }

    public UUID getBoardId() {
        return boardId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public List<String> getLabels() {
        return List.copyOf(labels);
    }

    public UUID getSwimlaneId() {
        return swimlaneId;
    }

    public UUID getStageId() {
        return stageId;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public List<CommentJpaEntity> getComments() {
        return comments;
    }

    public List<StageTransitionJpaEntity> getStageTransitions() {
        return stageTransitions;
    }

    public List<CardActivityRecordJpaEntity> getActivityLog() {
        return activityLog;
    }
}
