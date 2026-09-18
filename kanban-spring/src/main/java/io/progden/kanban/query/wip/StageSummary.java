package io.progden.kanban.query.wip;

import java.util.UUID;

/** {@link WipCalculator} 的輸入之一：一個 Stage 的顯示用資訊（名稱），依 board 的 Stage 順序排列。 */
public record StageSummary(UUID stageId, String name) {
}
