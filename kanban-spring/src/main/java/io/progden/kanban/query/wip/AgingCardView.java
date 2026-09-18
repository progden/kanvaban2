package io.progden.kanban.query.wip;

import java.util.UUID;

/** {@code uc-view-aging-wip} 的計算結果：一張進行中卡片的年齡（天數）。 */
public record AgingCardView(UUID cardId, String title, UUID stageId, long ageDays) {
}
