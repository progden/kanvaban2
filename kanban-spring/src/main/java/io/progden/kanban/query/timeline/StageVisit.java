package io.progden.kanban.query.timeline;

import java.time.Instant;
import java.util.UUID;

/**
 * 一次「卡片停留在某個 Stage」的區間（design-kanban-widgets.md 第 1 節 stageVisits）。
 * {@code leftAt} 為 {@code null} 表示卡片目前仍停留在這個 Stage。
 */
public record StageVisit(UUID stageId, Instant enteredAt, Instant leftAt) {
}
