package io.progden.kanban.core.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Card Aggregate Root：對應 spec-kanban-basic.md 的 {@code card}，獨立於 {@link Board}
 * 之外的 Aggregate，透過 {@code boardId}／{@code swimlaneId}／{@code stageId}（UUID）參照所屬的
 * Board/Swimlane/Stage，不持有物件參照（design-kanban-basic.md 第 2 節）。
 *
 * <p>刪除採「軟刪除」（{@code deleted} 旗標）：{@code uc-delete-card} post 要求刪除動作本身
 * 要被記錄一筆活動紀錄，若直接刪除資料列，活動紀錄會一併消失、違反 post；改為標記
 * {@code deleted=true} 並保留活動紀錄，讀取端（{@link CardLookupPort} 等）一律排除已刪除的卡片，
 * 使其「從看板中移除」（實作細節、非 spec 明訂行為，見 implementation-loop decision-log）。
 */
public final class Card {

    private final UUID id;
    private final UUID boardId;
    private String title;
    private String description;
    private LocalDate dueDate;
    private List<String> labels;
    private UUID swimlaneId;
    private UUID stageId;
    private boolean deleted;
    private final List<Comment> comments;
    private final List<StageTransition> stageTransitions;
    private final List<ActivityRecord> activityLog;

    private Card(UUID id, UUID boardId, String title, String description, LocalDate dueDate, List<String> labels,
            UUID swimlaneId, UUID stageId, boolean deleted, List<Comment> comments,
            List<StageTransition> stageTransitions, List<ActivityRecord> activityLog) {
        this.id = id;
        this.boardId = boardId;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.labels = labels;
        this.swimlaneId = swimlaneId;
        this.stageId = stageId;
        this.deleted = deleted;
        this.comments = comments;
        this.stageTransitions = stageTransitions;
        this.activityLog = activityLog;
    }

    public static Card create(UUID operatorId, UUID boardId, String title, CardPlacement placement, Instant now) {
        validateTitle(title);
        Card card = new Card(UUID.randomUUID(), boardId, title, null, null, List.of(),
                placement.swimlaneId(), placement.stageId(), false,
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        card.recordActivity(operatorId, "建立卡片「" + title + "」", now);
        return card;
    }

    public static Card reconstruct(UUID id, UUID boardId, String title, String description, LocalDate dueDate,
            List<String> labels, UUID swimlaneId, UUID stageId, boolean deleted,
            List<CommentSnapshot> commentSnapshots, List<StageTransitionSnapshot> transitionSnapshots,
            List<ActivityRecordSnapshot> activitySnapshots) {
        List<Comment> comments = new ArrayList<>();
        for (CommentSnapshot snapshot : commentSnapshots) {
            comments.add(Comment.reconstruct(snapshot.id(), snapshot.authorId(), snapshot.content(),
                    snapshot.createdAt()));
        }
        List<StageTransition> transitions = new ArrayList<>();
        for (StageTransitionSnapshot snapshot : transitionSnapshots) {
            transitions.add(StageTransition.reconstruct(snapshot.id(), snapshot.operatorId(),
                    snapshot.fromStageId(), snapshot.toStageId(), snapshot.occurredAt()));
        }
        List<ActivityRecord> activityLog = new ArrayList<>();
        for (ActivityRecordSnapshot snapshot : activitySnapshots) {
            activityLog.add(ActivityRecord.reconstruct(
                    snapshot.id(), snapshot.operatorId(), snapshot.action(), snapshot.occurredAt()));
        }
        return new Card(id, boardId, title, description, dueDate, labels == null ? List.of() : labels,
                swimlaneId, stageId, deleted, comments, transitions, activityLog);
    }

    // ---- 讀取 ----

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

    public List<Comment> getComments() {
        return List.copyOf(comments);
    }

    public List<StageTransition> getStageTransitions() {
        return List.copyOf(stageTransitions);
    }

    public List<ActivityRecord> getActivityLog() {
        return List.copyOf(activityLog);
    }

    // ---- 寫入 ----

    public void edit(UUID operatorId, CardDetails details, Instant now) {
        this.description = details.description();
        this.dueDate = details.dueDate();
        this.labels = details.labels() == null ? List.of() : List.copyOf(details.labels());
        recordActivity(operatorId, "編輯卡片內容", now);
    }

    public void moveToSwimlane(UUID operatorId, UUID swimlaneId, Instant now) {
        this.swimlaneId = swimlaneId;
        recordActivity(operatorId, "移動卡片至其他 Swimlane", now);
    }

    public void moveToStage(UUID operatorId, UUID stageId, Instant now) {
        UUID fromStageId = this.stageId;
        this.stageId = stageId;
        stageTransitions.add(new StageTransition(operatorId, fromStageId, stageId, now));
    }

    public Comment addComment(UUID operatorId, String content, Instant now) {
        Comment comment = Comment.create(operatorId, content, now);
        comments.add(comment);
        return comment;
    }

    public void delete(UUID operatorId, Instant now) {
        this.deleted = true;
        recordActivity(operatorId, "刪除卡片", now);
    }

    // ---- 私有輔助 ----

    private void recordActivity(UUID operatorId, String action, Instant now) {
        activityLog.add(new ActivityRecord(operatorId, action, now));
    }

    private static void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new DomainException(ErrorCode.EMPTY_CARD_TITLE, "卡片標題不可為空");
        }
    }
}
