package io.progden.kanban.spring.cucumber;

import io.progden.kanban.core.domain.CardLookupPort;
import io.progden.kanban.core.domain.CardSummary;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * {@link CardLookupPort} 的測試替身：Card Aggregate（T-03）尚未實作，無法透過真正的 API 建立卡片。
 * 這個替身讓 Cucumber step definitions 可以手動注入「某個看板目前有哪些卡片」的假資料，
 * 驗證 Board 端依卡片數量做的 Swimlane／Stage 刪除保護機制（SWIMLANE_HAS_CARDS／STAGE_HAS_CARDS）。
 * T-03 完成後，正式環境應改用查詢真正 Card persistence 的實作（見 {@code NoOpCardLookupPort}）。
 */
public class FakeCardLookupPort implements CardLookupPort {

    private final List<CardSummary> cards = new ArrayList<>();

    public void reset() {
        cards.clear();
    }

    public void addCard(UUID boardId, UUID swimlaneId, UUID stageId) {
        cards.add(new CardSummary(UUID.randomUUID(), swimlaneId, stageId));
    }

    public void moveAllCardsToStage(UUID fromStageId, UUID toStageId) {
        List<CardSummary> moved = new ArrayList<>();
        for (CardSummary card : cards) {
            if (card.stageId().equals(fromStageId)) {
                moved.add(new CardSummary(card.cardId(), card.swimlaneId(), toStageId));
            } else {
                moved.add(card);
            }
        }
        cards.clear();
        cards.addAll(moved);
    }

    public void removeAllCardsInSwimlane(UUID swimlaneId) {
        cards.removeIf(c -> c.swimlaneId().equals(swimlaneId));
    }

    @Override
    public List<CardSummary> findByBoardId(UUID boardId) {
        return List.copyOf(cards);
    }

    @TestConfiguration
    public static class Config {

        @Bean
        @Primary
        public FakeCardLookupPort fakeCardLookupPort() {
            return new FakeCardLookupPort();
        }
    }
}
