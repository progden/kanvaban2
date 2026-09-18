package io.progden.kanban.spring.persistence;

import io.progden.kanban.core.domain.CardLookupPort;
import io.progden.kanban.core.domain.CardSummary;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * {@link CardLookupPort} 的正式實作，查詢真正的 {@code Card} persistence（取代
 * {@code NoOpCardLookupPort}），讓 {@code Board} 的 Swimlane／Stage 刪除保護
 * （{@code SWIMLANE_HAS_CARDS}／{@code STAGE_HAS_CARDS}）對實際卡片生效。只回傳未被軟刪除的卡片。
 */
@Component
class CardLookupPortAdapter implements CardLookupPort {

    private final CardJpaRepository cardJpaRepository;

    CardLookupPortAdapter(CardJpaRepository cardJpaRepository) {
        this.cardJpaRepository = cardJpaRepository;
    }

    @Override
    public List<CardSummary> findByBoardId(UUID boardId) {
        return cardJpaRepository.findByBoardIdAndDeletedFalse(boardId).stream()
                .map(c -> new CardSummary(c.getId(), c.getSwimlaneId(), c.getStageId()))
                .toList();
    }
}
