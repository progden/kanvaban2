package io.progden.kanban.core.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * {@code Card} 的持久化 port，由 kanban-spring 的 persistence 層實作。
 *
 * <p>{@code findActiveBySwimlaneId}／{@code findActiveByStageId} 供應用層在協調
 * {@code uc-delete-swimlane}／{@code uc-delete-stage} 的卡片一併刪除／轉移流程時使用
 * （design-kanban-basic.md 第 7 節；只回傳未被軟刪除的卡片）。
 */
public interface CardRepository {

    void save(Card card);

    Optional<Card> findById(UUID cardId);

    List<Card> findActiveBySwimlaneId(UUID swimlaneId);

    List<Card> findActiveByStageId(UUID stageId);
}
