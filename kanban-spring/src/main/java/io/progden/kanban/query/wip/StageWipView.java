package io.progden.kanban.query.wip;

import java.util.UUID;

/** {@code uc-view-wip} 的計算結果：一個 Stage 目前的卡片數量。 */
public record StageWipView(UUID stageId, String stageName, int count) {
}
