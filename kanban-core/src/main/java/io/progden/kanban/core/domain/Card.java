package io.progden.kanban.core.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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
    private List<UUID> assigneeIds;
    private final List<Comment> comments;
    private final List<StageTransition> stageTransitions;
    private final List<ActivityRecord> activityLog;

    private Card(UUID id, UUID boardId, String title, String description, LocalDate dueDate, List<String> labels,
            UUID swimlaneId, UUID stageId, boolean deleted, List<UUID> assigneeIds, List<Comment> comments,
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
        this.assigneeIds = assigneeIds;
        this.comments = comments;
        this.stageTransitions = stageTransitions;
        this.activityLog = activityLog;
    }

    public static Card create(UUID operatorId, UUID boardId, String title, CardPlacement placement, Instant now) {
        validateTitle(title);
        Card card = new Card(UUID.randomUUID(), boardId, title, null, null, List.of(),
                placement.swimlaneId(), placement.stageId(), false, List.of(),
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        card.recordActivity(operatorId, "建立卡片「" + title + "」", now);
        return card;
    }

    public static Card reconstruct(UUID id, UUID boardId, String title, String description, LocalDate dueDate,
            List<String> labels, UUID swimlaneId, UUID stageId, boolean deleted, List<UUID> assigneeIds,
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
                swimlaneId, stageId, deleted, assigneeIds == null ? List.of() : assigneeIds,
                comments, transitions, activityLog);
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

    public List<UUID> getAssigneeIds() {
        return List.copyOf(assigneeIds);
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

    /**
     * 設定負責人集合（CR-002／uc-set-card-assignees／uc-assign-card-owner-by-drag 共用）。
     * {@code newAssigneeIds} 依呼叫端指定的順序去重後成為新的負責人名單；與現況相同（含拖曳重複追加
     * 已存在負責人的情境）時不記錄活動、也不視為變更，回傳 false。
     *
     * <p>{@code Card} 本身不持有 {@code User} 顯示名字，活動紀錄文字（例如「將卡片負責人設定為
     * 雅婷、建宏」「將 建宏 從卡片負責人中移除」）由呼叫端（{@code CardApplicationService}，已查過
     * 異動前後的成員名單與顯示名字）組好後傳入；{@code Card} 只負責「異動才記錄、沒異動不記錄」這個
     * 不變條件（design-user-membership.md 第 4 點同一慣例：格式化細節交給呼叫端）。
     */
    public boolean assignTo(UUID operatorId, List<UUID> newAssigneeIds, String actionText, Instant now) {
        List<UUID> deduped = dedupeIds(newAssigneeIds);
        if (deduped.equals(this.assigneeIds)) {
            return false;
        }
        this.assigneeIds = deduped;
        recordActivity(operatorId, actionText, now);
        return true;
    }

    /**
     * 因 {@code board-membership} 被移除而連帶清除該成員的負責人身分（uc-remove-member post 第 2 條）。
     * spec 沒有要求這個 cascade 動作本身產生 Card 的活動紀錄（活動紀錄記在 board-membership 那一筆），
     * 所以不呼叫 {@link #recordActivity}。
     */
    public void unassignMember(UUID userId) {
        this.assigneeIds = this.assigneeIds.stream().filter(id -> !id.equals(userId)).toList();
    }

    private List<UUID> dedupeIds(List<UUID> ids) {
        Set<UUID> seen = new LinkedHashSet<>();
        List<UUID> result = new ArrayList<>();
        for (UUID id : ids) {
            if (seen.add(id)) {
                result.add(id);
            }
        }
        return result;
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
