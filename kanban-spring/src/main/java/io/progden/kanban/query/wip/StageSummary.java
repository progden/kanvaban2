package io.progden.kanban.query.wip;

import io.progden.kanban.core.domain.StageRole;
import java.util.UUID;

/** {@link WipCalculator} 的輸入之一：一個 Stage 的顯示用資訊（名稱、角色），依 board 的 Stage 順序排列。 */
public record StageSummary(UUID stageId, String name, StageRole role) {
}
