package io.progden.kanban.query.widgets;

import io.progden.kanban.core.domain.Board;
import io.progden.kanban.core.domain.BoardRepository;
import io.progden.kanban.core.domain.DomainException;
import io.progden.kanban.core.domain.ErrorCode;
import io.progden.kanban.core.domain.Stage;
import io.progden.kanban.core.domain.StageRole;
import io.progden.kanban.query.duedate.DueDateReminder;
import io.progden.kanban.query.duedate.DueDateReminderView;
import io.progden.kanban.query.throughput.CfdCalculator;
import io.progden.kanban.query.throughput.CfdDataPoint;
import io.progden.kanban.query.throughput.ThroughputCalculator;
import io.progden.kanban.query.throughput.ThroughputEntry;
import io.progden.kanban.query.throughput.ThroughputGranularity;
import io.progden.kanban.query.timeline.CardTimeline;
import io.progden.kanban.query.timeline.CardTimelineProjector;
import io.progden.kanban.query.timeline.CardTimelineSource;
import io.progden.kanban.query.timeline.CycleLeadTimeCalculator;
import io.progden.kanban.query.timeline.CycleLeadTimeView;
import io.progden.kanban.query.timeline.StageTransitionRecord;
import io.progden.kanban.query.wip.AgingCalculator;
import io.progden.kanban.query.wip.AgingCardView;
import io.progden.kanban.query.wip.StageSummary;
import io.progden.kanban.query.wip.StageWipView;
import io.progden.kanban.query.wip.WipCalculator;
import io.progden.kanban.spring.persistence.CardActivityRecordJpaEntity;
import io.progden.kanban.spring.persistence.CardJpaEntity;
import io.progden.kanban.spring.persistence.CardJpaRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * spec-kanban-widgets.md 六個 {@code uc-view-*} 對應的跨 aggregate 讀取投影：只讀 {@code board}
 * 的 Stage 設定／事件歷史與 {@code card} 的目前狀態（Aggregate 標記說明），重播出
 * {@link CardTimeline} 交給各 Feature 的純函式計算器（design-kanban-widgets.md）。
 */
@Service
public class KanbanWidgetsQueryService {

    private final BoardRepository boardRepository;
    private final CardJpaRepository cardJpaRepository;

    public KanbanWidgetsQueryService(BoardRepository boardRepository, CardJpaRepository cardJpaRepository) {
        this.boardRepository = boardRepository;
        this.cardJpaRepository = cardJpaRepository;
    }

    @Transactional(readOnly = true)
    public CycleLeadTimeView viewCycleLeadTime(UUID boardId) {
        Board board = loadBoard(boardId);
        return CycleLeadTimeCalculator.calculate(loadTimelines(boardId, board));
    }

    @Transactional(readOnly = true)
    public List<StageWipView> viewWip(UUID boardId) {
        Board board = loadBoard(boardId);
        List<CardTimeline> timelines = loadTimelines(boardId, board);
        List<StageSummary> stages = board.getStages().stream()
                .map(s -> new StageSummary(s.getId(), s.getName(), s.getRole()))
                .toList();
        return WipCalculator.countByStage(timelines, stages);
    }

    @Transactional(readOnly = true)
    public List<AgingCardView> viewAgingWip(UUID boardId) {
        Board board = loadBoard(boardId);
        List<CardTimeline> timelines = loadTimelines(boardId, board);
        return AgingCalculator.age(timelines, board.getClockTime(Instant.now()));
    }

    @Transactional(readOnly = true)
    public List<ThroughputEntry> viewThroughput(UUID boardId, ThroughputGranularity granularity) {
        Board board = loadBoard(boardId);
        return ThroughputCalculator.countByPeriod(loadTimelines(boardId, board), granularity);
    }

    @Transactional(readOnly = true)
    public List<CfdDataPoint> viewCfd(UUID boardId) {
        Board board = loadBoard(boardId);
        List<CardTimeline> timelines = loadTimelines(boardId, board);
        List<UUID> stageIdsInOrder = board.getStages().stream().map(Stage::getId).toList();
        return CfdCalculator.cumulativeByStageAndDate(timelines, stageIdsInOrder, board.getClockTime(Instant.now()));
    }

    /**
     * {@code thresholdDays} 為 {@code null} 時只計算「已逾期」清單（不需要門檻天數也能檢視，見
     * spec Scenario「檢視已逾期的卡片」），「即將到期」清單維持空清單；有提供時依 fail p1 驗證範圍。
     */
    @Transactional(readOnly = true)
    public DueDateReminderView viewDueDateReminder(UUID boardId, Integer thresholdDays) {
        Board board = loadBoard(boardId);
        List<CardTimeline> timelines = loadTimelines(boardId, board);
        LocalDate asOfDate = board.getClockTime(Instant.now()).atZone(ZoneOffset.UTC).toLocalDate();

        List<io.progden.kanban.query.duedate.DueDateCardView> overdue = DueDateReminder.overdue(timelines, asOfDate);
        List<io.progden.kanban.query.duedate.DueDateCardView> upcoming = List.of();
        if (thresholdDays != null) {
            if (thresholdDays < 1 || thresholdDays > 365) {
                throw new DomainException(
                        ErrorCode.INVALID_DUEDATE_THRESHOLD_DAYS, "門檻天數必須是 1 到 365 之間的正整數");
            }
            upcoming = DueDateReminder.upcoming(timelines, asOfDate, thresholdDays);
        }
        return new DueDateReminderView(overdue, upcoming);
    }

    private Board loadBoard(UUID boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new DomainException(ErrorCode.BOARD_NOT_FOUND, "找不到指定的看板"));
    }

    private List<CardTimeline> loadTimelines(UUID boardId, Board board) {
        Map<UUID, StageRole> roleByStageId = board.getStages().stream()
                .collect(Collectors.toMap(Stage::getId, Stage::getRole));
        return cardJpaRepository.findByBoardIdAndDeletedFalse(boardId).stream()
                .map(card -> toTimeline(card, roleByStageId))
                .toList();
    }

    private CardTimeline toTimeline(CardJpaEntity card, Map<UUID, StageRole> roleByStageId) {
        Instant createdAt = card.getActivityLog().stream()
                .map(CardActivityRecordJpaEntity::getOccurredAt)
                .min(Instant::compareTo)
                .orElseThrow(() -> new IllegalStateException("卡片缺少建立活動紀錄，無法推算建立時間：" + card.getId()));
        List<StageTransitionRecord> transitions = card.getStageTransitions().stream()
                .map(t -> new StageTransitionRecord(t.getFromStageId(), t.getToStageId(), t.getOccurredAt()))
                .toList();
        CardTimelineSource source = new CardTimelineSource(card.getId(), card.getBoardId(), card.getTitle(),
                createdAt, card.getDueDate(), card.getStageId(), transitions);
        return CardTimelineProjector.project(source, roleByStageId);
    }
}
