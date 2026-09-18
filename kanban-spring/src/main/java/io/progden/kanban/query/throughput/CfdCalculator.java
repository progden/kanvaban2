package io.progden.kanban.query.throughput;

import io.progden.kanban.query.timeline.CardTimeline;
import io.progden.kanban.query.timeline.StageVisit;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * {@code uc-view-cfd} 的計算邏輯：純函式，標準 CFD 語意——卡片一旦（首次）進入某 Stage，該 Stage
 * 從進入當天起的累積計數即視為包含這張卡片，即使卡片後續已離開該 Stage（design-kanban-widgets.md
 * 第 7 節）。日期範圍起點取所有卡片最早的到達日期，終點為 {@code asOf} 所在日期，日期分組採 UTC。
 */
public final class CfdCalculator {

    private CfdCalculator() {
    }

    public static List<CfdDataPoint> cumulativeByStageAndDate(
            List<CardTimeline> timelines, List<UUID> stageIdsInOrder, Instant asOf) {
        Map<UUID, List<LocalDate>> earliestEntryDatesByStage = new HashMap<>();
        LocalDate minDate = null;
        for (CardTimeline timeline : timelines) {
            Map<UUID, LocalDate> earliestPerStage = new HashMap<>();
            for (StageVisit visit : timeline.stageVisits()) {
                LocalDate date = visit.enteredAt().atZone(ZoneOffset.UTC).toLocalDate();
                earliestPerStage.merge(visit.stageId(), date, (a, b) -> a.isBefore(b) ? a : b);
                if (minDate == null || date.isBefore(minDate)) {
                    minDate = date;
                }
            }
            for (Map.Entry<UUID, LocalDate> entry : earliestPerStage.entrySet()) {
                earliestEntryDatesByStage.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).add(entry.getValue());
            }
        }

        LocalDate endDate = asOf.atZone(ZoneOffset.UTC).toLocalDate();
        if (minDate == null) {
            minDate = endDate;
        }

        List<CfdDataPoint> dataPoints = new ArrayList<>();
        for (LocalDate date = minDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            Map<UUID, Integer> countByStage = new LinkedHashMap<>();
            for (UUID stageId : stageIdsInOrder) {
                List<LocalDate> entryDates = earliestEntryDatesByStage.getOrDefault(stageId, List.of());
                LocalDate cutoff = date;
                long count = entryDates.stream().filter(d -> !d.isAfter(cutoff)).count();
                countByStage.put(stageId, (int) count);
            }
            dataPoints.add(new CfdDataPoint(date, countByStage));
        }
        return dataPoints;
    }
}
