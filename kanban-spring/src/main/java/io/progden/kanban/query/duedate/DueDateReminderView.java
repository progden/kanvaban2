package io.progden.kanban.query.duedate;

import java.util.List;

/**
 * {@code uc-view-duedate-reminder} 的計算結果。{@code upcoming} 只有在呼叫端提供門檻天數時才計算，
 * 未提供時為空清單（「已逾期」清單不需要門檻天數也能檢視，見 decision-log.md）。
 */
public record DueDateReminderView(List<DueDateCardView> overdue, List<DueDateCardView> upcoming) {
}
