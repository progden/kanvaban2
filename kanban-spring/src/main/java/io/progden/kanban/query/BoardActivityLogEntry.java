package io.progden.kanban.query;

import java.time.Instant;
import java.util.UUID;

/**
 * {@code uc-view-board-activity-log} 用的合併活動紀錄項目：{@code Board} 自己的活動紀錄
 * （Swimlane／Stage／建立看板）與 {@code board-membership} 的活動紀錄（邀請／變更角色／移除成員）
 * 合併後、依時間新到舊排序的其中一筆。
 */
public record BoardActivityLogEntry(UUID operatorId, String action, Instant occurredAt) {
}
