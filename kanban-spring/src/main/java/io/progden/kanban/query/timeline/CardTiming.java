package io.progden.kanban.query.timeline;

import java.time.Instant;
import java.util.UUID;

/**
 * 一張已完成卡片的 Lead Time／Cycle Time（{@code uc-view-cycle-lead-time} post p1）。
 * {@code cycleTimeDays} 為 {@code null} 表示這張卡片未曾進入 Start 角色的 Stage（post p2），
 * 顯示為「無」，不計入 Cycle Time 統計摘要。{@code doneAt} 是最後一次進入 Done 的時間
 * （post p4：離開 Done 後再次完成，以最後一次進入 Done 的時間為準）。
 */
public record CardTiming(UUID cardId, String title, long leadTimeDays, Long cycleTimeDays, Instant doneAt) {
}
