package io.progden.kanban.spring.persistence;

import io.progden.kanban.core.domain.CardLookupPort;
import io.progden.kanban.core.domain.CardSummary;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * {@link CardLookupPort} 的暫時實作：{@code Card} Aggregate（T-03）尚未存在，一律回傳空清單，
 * 代表目前任何 Swimlane／Stage 底下都沒有卡片。T-03 完成後要換成查詢 Card persistence 的真正實作，
 * 屆時 Swimlane/Stage 刪除保護（{@code SWIMLANE_HAS_CARDS}／{@code STAGE_HAS_CARDS}）才會對實際卡片生效。
 */
@Component
class NoOpCardLookupPort implements CardLookupPort {

    @Override
    public List<CardSummary> findByBoardId(UUID boardId) {
        return List.of();
    }
}
