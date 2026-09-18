package io.progden.kanban.query.timeline;

import io.progden.kanban.core.domain.StageRole;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * design-kanban-widgets.md 第 1、5 節描述的重播演算法：純函式，只依賴呼叫端傳入的資料與
 * Stage 角色對照表，不接觸 persistence，方便單元測試（同 {@code FeatureCrBoardCalculator} 的原則）。
 */
public final class CardTimelineProjector {

    private CardTimelineProjector() {
    }

    /**
     * 重建卡片時間軸。{@code roleByStageId} 只在查詢當下讀取（CR-003 的 Stage 角色可能與投影的
     * 建立時間不同步），不寫進投影，重新指派角色不需要重建投影（design-kanban-widgets.md 第 1 節）。
     */
    public static CardTimeline project(CardTimelineSource source, Map<UUID, StageRole> roleByStageId) {
        List<StageTransitionRecord> transitions = source.transitions();
        UUID initialStageId = transitions.isEmpty() ? source.currentStageId() : transitions.get(0).fromStageId();

        List<StageVisit> visits = new ArrayList<>();
        UUID stageId = initialStageId;
        Instant enteredAt = source.createdAt();
        for (StageTransitionRecord transition : transitions) {
            visits.add(new StageVisit(stageId, enteredAt, transition.occurredAt()));
            stageId = transition.toStageId();
            enteredAt = transition.occurredAt();
        }
        visits.add(new StageVisit(stageId, enteredAt, null));

        Instant startedAt = null;
        Instant doneAt = null;
        for (StageVisit visit : visits) {
            StageRole role = roleByStageId.getOrDefault(visit.stageId(), StageRole.NONE);
            if (startedAt == null && role == StageRole.START) {
                startedAt = visit.enteredAt();
            }
            doneAt = role == StageRole.DONE ? visit.enteredAt() : null;
        }

        StageVisit currentVisit = visits.get(visits.size() - 1);
        return new CardTimeline(source.cardId(), source.boardId(), source.title(), source.createdAt(),
                startedAt, doneAt, currentVisit.stageId(), currentVisit.enteredAt(), List.copyOf(visits),
                source.dueDate());
    }
}
