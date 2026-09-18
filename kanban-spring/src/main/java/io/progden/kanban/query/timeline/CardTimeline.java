package io.progden.kanban.query.timeline;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * 跨 aggregate 讀取投影（design-kanban-widgets.md 第 1 節）：由 {@code card} 的
 * {@code stageTransitions}／活動紀錄重播出的單張卡片時間軸，是 F03 四個 Feature 共用的資料來源。
 * 已被軟刪除的卡片不會出現在這份投影中（呼叫端查詢時已排除，見 {@code KanbanWidgetsQueryService}）。
 */
public record CardTimeline(
        UUID cardId,
        UUID boardId,
        String title,
        Instant createdAt,
        Instant startedAt,
        Instant doneAt,
        UUID currentStageId,
        Instant enteredCurrentStageAt,
        List<StageVisit> stageVisits,
        LocalDate dueDate) {
}
