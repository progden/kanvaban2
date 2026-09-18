package io.progden.kanban.query.timeline;

import java.util.List;

/** {@code uc-view-cycle-lead-time} 的計算結果。 */
public record CycleLeadTimeView(
        List<CardTiming> cards, PercentileSummary leadTime, PercentileSummary cycleTime, int excludedCycleTimeCount) {
}
