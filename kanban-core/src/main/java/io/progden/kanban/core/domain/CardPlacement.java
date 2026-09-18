package io.progden.kanban.core.domain;

import java.util.UUID;

/**
 * {@link Card#create} 用的參數物件：卡片建立時所在的 Swimlane／Stage 交會格
 * （design-kanban-basic.md 5a 節，為符合方法參數數量限制而拆出）。
 */
public record CardPlacement(UUID swimlaneId, UUID stageId) {
}
