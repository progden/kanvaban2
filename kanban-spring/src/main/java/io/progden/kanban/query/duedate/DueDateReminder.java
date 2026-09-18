package io.progden.kanban.query.duedate;

import io.progden.kanban.query.timeline.CardTimeline;
import java.time.LocalDate;
import java.util.List;

/**
 * {@code uc-view-duedate-reminder} 的計算邏輯：純函式，只考慮尚未完成（{@code doneAt == null}）
 * 且有設定 {@code dueDate} 的卡片（design-kanban-widgets.md 第 6 節）。
 *
 * <p>「已逾期」與「即將到期」互斥：已逾期取 {@code dueDate < asOfDate}，即將到期取
 * {@code dueDate ∈ [asOfDate, asOfDate + thresholdDays]}，避免同一張卡片同時出現在兩份清單
 * （低風險實作決定，見 decision-log.md）。
 */
public final class DueDateReminder {

    private DueDateReminder() {
    }

    public static List<DueDateCardView> overdue(List<CardTimeline> timelines, LocalDate asOfDate) {
        return timelines.stream()
                .filter(t -> t.doneAt() == null && t.dueDate() != null && t.dueDate().isBefore(asOfDate))
                .map(t -> new DueDateCardView(t.cardId(), t.title(), t.dueDate()))
                .toList();
    }

    public static List<DueDateCardView> upcoming(List<CardTimeline> timelines, LocalDate asOfDate, int thresholdDays) {
        LocalDate limit = asOfDate.plusDays(thresholdDays);
        return timelines.stream()
                .filter(t -> t.doneAt() == null && t.dueDate() != null
                        && !t.dueDate().isBefore(asOfDate) && !t.dueDate().isAfter(limit))
                .map(t -> new DueDateCardView(t.cardId(), t.title(), t.dueDate()))
                .toList();
    }
}
