package io.progden.kanban.spring.web;

import io.progden.kanban.query.duedate.DueDateReminderView;
import io.progden.kanban.query.throughput.CfdDataPoint;
import io.progden.kanban.query.timeline.CycleLeadTimeView;
import io.progden.kanban.query.wip.AgingCardView;
import io.progden.kanban.query.wip.StageWipView;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * F03 六個 {@code uc-view-*} 的 web 回應型別，一個檔案集中放置（皆為 query 層結果的薄轉換，
 * 比照 {@code FeatureCrBoardResponse} 的作法，但因為六個回應都只是 1:1 轉型別，集中一檔避免六個
 * 幾乎相同的小檔案，屬低風險技術決定，見 decision-log.md）。
 */
final class KanbanWidgetsResponses {

    private KanbanWidgetsResponses() {
    }

    record CycleLeadTimeResponse(List<CardTimingView> cards, PercentileView leadTime, PercentileView cycleTime,
            int excludedCycleTimeCount) {

        static CycleLeadTimeResponse from(CycleLeadTimeView view) {
            List<CardTimingView> cards = view.cards().stream()
                    .map(c -> new CardTimingView(c.cardId(), c.title(), c.leadTimeDays(), c.cycleTimeDays(), c.doneAt()))
                    .toList();
            return new CycleLeadTimeResponse(cards, PercentileView.from(view.leadTime()),
                    PercentileView.from(view.cycleTime()), view.excludedCycleTimeCount());
        }
    }

    record CardTimingView(UUID cardId, String title, long leadTimeDays, Long cycleTimeDays,
            java.time.Instant doneAt) {
    }

    record PercentileView(long p50, long p85, long p95) {
        static PercentileView from(io.progden.kanban.query.timeline.PercentileSummary summary) {
            return new PercentileView(summary.p50(), summary.p85(), summary.p95());
        }
    }

    record WipResponse(List<StageWipEntry> stages) {
        static WipResponse from(List<StageWipView> views) {
            return new WipResponse(views.stream()
                    .map(v -> new StageWipEntry(v.stageId(), v.stageName(), v.count()))
                    .toList());
        }
    }

    record StageWipEntry(UUID stageId, String stageName, int count) {
    }

    record AgingWipResponse(List<AgingCardEntry> cards) {
        static AgingWipResponse from(List<AgingCardView> views) {
            return new AgingWipResponse(views.stream()
                    .map(v -> new AgingCardEntry(v.cardId(), v.title(), v.stageId(), v.ageDays()))
                    .toList());
        }
    }

    record AgingCardEntry(UUID cardId, String title, UUID stageId, long ageDays) {
    }

    record ThroughputResponse(List<ThroughputPeriodEntry> periods) {
        static ThroughputResponse from(List<io.progden.kanban.query.throughput.ThroughputEntry> entries) {
            return new ThroughputResponse(entries.stream()
                    .map(e -> new ThroughputPeriodEntry(e.periodStart(), e.count()))
                    .toList());
        }
    }

    record ThroughputPeriodEntry(LocalDate periodStart, int count) {
    }

    record CfdResponse(List<CfdEntry> dataPoints) {
        static CfdResponse from(List<CfdDataPoint> points) {
            return new CfdResponse(points.stream()
                    .map(p -> new CfdEntry(p.date(), p.countByStage()))
                    .toList());
        }
    }

    record CfdEntry(LocalDate date, Map<UUID, Integer> countByStage) {
    }

    record DueDateReminderResponse(List<DueDateCardEntry> overdue, List<DueDateCardEntry> upcoming) {
        static DueDateReminderResponse from(DueDateReminderView view) {
            return new DueDateReminderResponse(
                    view.overdue().stream().map(DueDateCardEntry::from).toList(),
                    view.upcoming().stream().map(DueDateCardEntry::from).toList());
        }
    }

    record DueDateCardEntry(UUID cardId, String title, LocalDate dueDate) {
        static DueDateCardEntry from(io.progden.kanban.query.duedate.DueDateCardView view) {
            return new DueDateCardEntry(view.cardId(), view.title(), view.dueDate());
        }
    }
}
