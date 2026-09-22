package io.progden.kanban.query.wip;

import io.progden.kanban.core.domain.StageRole;
import io.progden.kanban.query.timeline.CardTimeline;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * {@code uc-view-wip} 的計算邏輯：純函式，依 {@link CardTimeline#currentStageId} 分組計數
 * （design-kanban-widgets.md 第 6 節）。
 *
 * <p>post p1「不含 Done 角色 Stage」（CR-012）：排除 Done 角色的 Stage，不計入回傳結果。
 */
public final class WipCalculator {

    private WipCalculator() {
    }

    public static List<StageWipView> countByStage(List<CardTimeline> timelines, List<StageSummary> stagesInOrder) {
        Map<UUID, Long> countsByStage = timelines.stream()
                .collect(Collectors.groupingBy(CardTimeline::currentStageId, Collectors.counting()));
        return stagesInOrder.stream()
                .filter(s -> s.role() != StageRole.DONE)
                .map(s -> new StageWipView(s.stageId(), s.name(), countsByStage.getOrDefault(s.stageId(), 0L).intValue()))
                .toList();
    }
}
