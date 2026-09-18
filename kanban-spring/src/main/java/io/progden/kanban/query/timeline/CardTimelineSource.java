package io.progden.kanban.query.timeline;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * {@link CardTimelineProjector#project} 的輸入：一張卡片重建時間軸所需的最少欄位，由呼叫端
 * （persistence 層）從 {@code CardJpaEntity} 組出，讓投影邏輯本身維持純函式、不依賴 JPA
 * （design-kanban-widgets.md 第 6 節）。
 *
 * <p>{@code createdAt} 取自該卡片活動紀錄中最早的一筆（card 沒有獨立的 {@code createdAt} 欄位，
 * design-kanban-widgets.md 第 5 節原建議在 {@code Card} domain 補欄位，但 F03 任務範圍不可修改
 * F01 的 domain 程式碼，改用既有活動紀錄推算，效果等價）。{@code transitions} 需依
 * {@code occurredAt} 由舊到新排序。
 */
public record CardTimelineSource(
        UUID cardId,
        UUID boardId,
        String title,
        Instant createdAt,
        LocalDate dueDate,
        UUID currentStageId,
        List<StageTransitionRecord> transitions) {
}
