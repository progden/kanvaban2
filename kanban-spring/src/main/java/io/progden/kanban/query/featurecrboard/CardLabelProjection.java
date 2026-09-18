package io.progden.kanban.query.featurecrboard;

import io.progden.kanban.core.domain.StageRole;
import java.util.List;
import java.util.UUID;

/**
 * {@link FeatureCrBoardCalculator} 的輸入：一張 {@code card} 需要的最少欄位（標籤、標題、所在
 * Stage 的角色），由呼叫端（persistence 層）組出。
 */
public record CardLabelProjection(UUID cardId, String title, List<String> labels, StageRole stageRole) {
}
