package io.progden.kanban.core.domain;

import java.util.UUID;

/**
 * {@code Card} 的唯讀投影，只給 {@link Board} 判斷「某個 Swimlane/Stage 底下還有沒有卡片」用，
 * 不含卡片完整欄位（design-kanban-basic.md 第 7 節）。
 */
public record CardSummary(UUID cardId, UUID swimlaneId, UUID stageId) {
}
