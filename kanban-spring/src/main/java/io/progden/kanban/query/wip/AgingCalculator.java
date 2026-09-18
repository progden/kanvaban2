package io.progden.kanban.query.wip;

import io.progden.kanban.query.timeline.CardTimeline;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code uc-view-aging-wip} 的計算邏輯：純函式，只對已進入 Start、尚未完成（{@code doneAt == null}）
 * 的卡片計算 {@code asOf - startedAt}（design-kanban-widgets.md 第 6 節），{@code asOf} 取自
 * Board Clock 目前時間（呼叫端傳入，對應 CR-004）。
 *
 * <p>天數採「日曆日差」（UTC {@code LocalDate} 相減），與 {@code CycleLeadTimeCalculator} 同一原則
 * （見該類別註解）。
 */
public final class AgingCalculator {

    private AgingCalculator() {
    }

    public static List<AgingCardView> age(List<CardTimeline> timelines, Instant asOf) {
        List<AgingCardView> result = new ArrayList<>();
        for (CardTimeline timeline : timelines) {
            if (timeline.startedAt() != null && timeline.doneAt() == null) {
                long ageDays = ChronoUnit.DAYS.between(
                        timeline.startedAt().atZone(ZoneOffset.UTC).toLocalDate(),
                        asOf.atZone(ZoneOffset.UTC).toLocalDate());
                result.add(new AgingCardView(timeline.cardId(), timeline.title(), timeline.currentStageId(), ageDays));
            }
        }
        return result;
    }
}
