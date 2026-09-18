package io.progden.kanban.query.wip;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.progden.kanban.query.timeline.CardTimeline;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * {@link WipCalculator} 純函式單元測試，對應 spec-kanban-widgets.md {@code uc-view-wip} post p1、
 * Scenario「檢視各 Stage 目前的卡片數量」。
 */
class WipCalculatorTest {

    @Test
    void countsCardsByCurrentStageIncludingDone() {
        UUID todo = UUID.randomUUID();
        UUID inProgress = UUID.randomUUID();
        UUID done = UUID.randomUUID();
        List<StageSummary> stages = List.of(
                new StageSummary(todo, "待辦"), new StageSummary(inProgress, "進行中"), new StageSummary(done, "完成"));
        List<CardTimeline> timelines = List.of(
                cardAt(todo), cardAt(todo), cardAt(todo),
                cardAt(inProgress), cardAt(inProgress),
                cardAt(done), cardAt(done), cardAt(done), cardAt(done), cardAt(done));

        List<StageWipView> result = WipCalculator.countByStage(timelines, stages);

        assertEquals(3, result.get(0).count());
        assertEquals(2, result.get(1).count());
        assertEquals(5, result.get(2).count());
    }

    private static CardTimeline cardAt(UUID stageId) {
        Instant now = LocalDate.parse("2026-09-01").atStartOfDay(ZoneOffset.UTC).toInstant();
        return new CardTimeline(UUID.randomUUID(), UUID.randomUUID(), "卡片", now, null, null, stageId, now,
                List.of(), null);
    }
}
