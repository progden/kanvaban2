package io.progden.kanban.query.wip;

import io.progden.kanban.query.timeline.CardTimeline;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * {@code uc-view-wip} 的計算邏輯：純函式，依 {@link CardTimeline#currentStageId} 分組計數
 * （design-kanban-widgets.md 第 6 節）。
 *
 * <p>「其他名詞」表定義 WIP 為「目前不在 Done 角色 Stage 的卡片數量」，但本 Feature 的 Scenario
 * 「檢視各 Stage 目前的卡片數量」明確要求連 Done 角色的 Stage 也要顯示卡片數（含 Stage "完成"）；
 * Scenario／post 是驗收依據，優先於「其他名詞」的概述文字，故本計算器不排除 Done 角色 Stage
 * （低風險實作決定，見 decision-log.md）。
 */
public final class WipCalculator {

    private WipCalculator() {
    }

    public static List<StageWipView> countByStage(List<CardTimeline> timelines, List<StageSummary> stagesInOrder) {
        Map<UUID, Long> countsByStage = timelines.stream()
                .collect(Collectors.groupingBy(CardTimeline::currentStageId, Collectors.counting()));
        return stagesInOrder.stream()
                .map(s -> new StageWipView(s.stageId(), s.name(), countsByStage.getOrDefault(s.stageId(), 0L).intValue()))
                .toList();
    }
}
