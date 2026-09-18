package io.progden.kanban.core.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@code Board}（含內部 {@code Swimlane}／{@code Stage}）不變條件單元測試，
 * 對應 spec-kanban-basic.md「Swimlane 管理」「Stage（階段）管理」兩個 Feature。
 *
 * <p>所有寫入方法都改為接受呼叫端傳入的 {@code systemNow}（見 T-05 board-clock 的重構，design-
 * board-clock.md 第 2 節），這裡一律用 {@link Instant#now()} 模擬呼叫端讀到的系統時間。
 */
class BoardTest {

    private final UUID operatorId = UUID.randomUUID();

    @Test
    void should_createBoardWithDefaultSwimlaneAndStages_when_creating() {
        Board board = Board.create(operatorId, "產品開發看板", null, Instant.now());

        assertEquals(operatorId, board.getCreatedBy());
        assertEquals(1, board.getSwimlanes().size());
        assertEquals("預設泳道", board.getSwimlanes().get(0).getName());
        assertEquals(List.of("待辦", "進行中", "完成"),
                board.getStages().stream().map(Stage::getName).toList());
        assertEquals(1, board.getActivityLog().size());
        assertEquals("建立看板", board.getActivityLog().get(0).getAction());
        assertEquals(operatorId, board.getActivityLog().get(0).getOperatorId());
    }

    @Test
    void should_rejectCreate_when_nameIsBlank() {
        DomainException exception = assertThrows(DomainException.class,
                () -> Board.create(operatorId, " ", null, Instant.now()));

        assertEquals(ErrorCode.BOARD_NAME_BLANK, exception.getCode());
    }

    @Test
    void should_addSwimlaneAtBottom_when_addingSwimlane() {
        Board board = Board.create(operatorId, "看板", null, Instant.now());

        board.addSwimlane(operatorId, "緊急項目", Instant.now());

        assertEquals(2, board.getSwimlanes().size());
        assertEquals("緊急項目", board.getSwimlanes().get(1).getName());
        assertEquals(2, board.getActivityLog().size());
    }

    @Test
    void should_rejectAddSwimlane_when_nameIsBlank() {
        Board board = Board.create(operatorId, "看板", null, Instant.now());

        DomainException exception = assertThrows(DomainException.class,
                () -> board.addSwimlane(operatorId, "", Instant.now()));

        assertEquals(ErrorCode.EMPTY_SWIMLANE_NAME, exception.getCode());
        assertEquals("Swimlane 名稱不可為空", exception.getMessage());
        assertEquals(1, board.getSwimlanes().size());
    }

    @Test
    void should_renameSwimlane_when_swimlaneExists() {
        Board board = Board.create(operatorId, "看板", null, Instant.now());
        UUID swimlaneId = board.addSwimlane(operatorId, "緊急項目", Instant.now()).getId();
        int countBefore = board.getActivityLog().size();

        board.renameSwimlane(operatorId, swimlaneId, "本週優先", Instant.now());

        assertEquals("本週優先", board.getSwimlanes().get(1).getName());
        assertEquals(countBefore + 1, board.getActivityLog().size());
        assertEquals(operatorId, board.getActivityLog().get(board.getActivityLog().size() - 1).getOperatorId());
    }

    @Test
    void should_reorderSwimlanes_when_movingSwimlaneBeforeAnother() {
        Board board = Board.create(operatorId, "看板", null, Instant.now());
        UUID a = board.getSwimlanes().get(0).getId();
        UUID b = board.addSwimlane(operatorId, "B", Instant.now()).getId();
        UUID c = board.addSwimlane(operatorId, "C", Instant.now()).getId();
        int countBefore = board.getActivityLog().size();

        board.moveSwimlaneBefore(operatorId, c, a, Instant.now());

        assertEquals(List.of(c, a, b), board.getSwimlanes().stream().map(Swimlane::getId).toList());
        assertEquals(List.of(1, 2, 3), board.getSwimlanes().stream().map(Swimlane::getOrder).toList());
        assertEquals(countBefore + 1, board.getActivityLog().size());
        assertEquals(operatorId, board.getActivityLog().get(board.getActivityLog().size() - 1).getOperatorId());
    }

    @Test
    void should_removeEmptySwimlane_when_notTheLastOne() {
        Board board = Board.create(operatorId, "看板", null, Instant.now());
        UUID swimlaneId = board.addSwimlane(operatorId, "測試泳道", Instant.now()).getId();
        int countBefore = board.getActivityLog().size();

        board.removeSwimlane(operatorId, swimlaneId, Instant.now());

        assertEquals(1, board.getSwimlanes().size());
        assertEquals(countBefore + 1, board.getActivityLog().size());
        assertEquals(operatorId, board.getActivityLog().get(board.getActivityLog().size() - 1).getOperatorId());
    }

    @Test
    void should_rejectRemoveSwimlane_when_onlyOneSwimlaneLeft() {
        Board board = Board.create(operatorId, "看板", null, Instant.now());
        UUID swimlaneId = board.getSwimlanes().get(0).getId();

        DomainException exception = assertThrows(DomainException.class,
                () -> board.removeSwimlane(operatorId, swimlaneId, Instant.now()));

        assertEquals(ErrorCode.MINIMUM_SWIMLANE, exception.getCode());
        assertEquals("看板至少需要保留一個 Swimlane", exception.getMessage());
    }

    @Test
    void should_rejectRemoveSwimlane_when_stillHasCards() {
        UUID swimlaneId = UUID.randomUUID();
        FakeCardLookupPort port = new FakeCardLookupPort();
        Board board = Board.create(operatorId, "看板", port, Instant.now());
        board.addSwimlane(operatorId, "本週優先", Instant.now());
        UUID targetSwimlaneId = board.getSwimlanes().get(1).getId();
        port.addCard(UUID.randomUUID(), targetSwimlaneId, board.getStages().get(0).getId());
        board.refreshCardSummaries();

        DomainException exception = assertThrows(DomainException.class,
                () -> board.removeSwimlane(operatorId, targetSwimlaneId, Instant.now()));

        assertEquals(ErrorCode.SWIMLANE_HAS_CARDS, exception.getCode());
        assertTrue(exception.getMessage().contains("1"));
    }

    @Test
    void should_addStageAtEnd_when_positionNotSpecified() {
        Board board = Board.create(operatorId, "看板", null, Instant.now());
        int countBefore = board.getActivityLog().size();

        board.addStage(operatorId, "驗收中", null, Instant.now());

        assertEquals(List.of("待辦", "進行中", "完成", "驗收中"),
                board.getStages().stream().map(Stage::getName).toList());
        assertEquals(countBefore + 1, board.getActivityLog().size());
        assertEquals(operatorId, board.getActivityLog().get(board.getActivityLog().size() - 1).getOperatorId());
    }

    @Test
    void should_insertStage_when_positionSpecified() {
        Board board = Board.create(operatorId, "看板", null, Instant.now());
        UUID doneStageId = board.getStages().get(2).getId();
        int countBefore = board.getActivityLog().size();

        board.addStage(operatorId, "驗收中", doneStageId, Instant.now());

        assertEquals(List.of("待辦", "進行中", "驗收中", "完成"),
                board.getStages().stream().map(Stage::getName).toList());
        assertEquals(countBefore + 1, board.getActivityLog().size());
    }

    @Test
    void should_renameStage_when_stageExists() {
        Board board = Board.create(operatorId, "看板", null, Instant.now());
        UUID todoStageId = board.getStages().get(0).getId();
        int countBefore = board.getActivityLog().size();

        board.renameStage(operatorId, todoStageId, "規劃中", Instant.now());

        assertEquals("規劃中", board.getStages().get(0).getName());
        assertEquals(countBefore + 1, board.getActivityLog().size());
        assertEquals(operatorId, board.getActivityLog().get(board.getActivityLog().size() - 1).getOperatorId());
    }

    @Test
    void should_reorderStages_when_movingStageBeforeAnother() {
        Board board = Board.create(operatorId, "看板", null, Instant.now());
        UUID todo = board.getStages().get(0).getId();
        UUID doing = board.getStages().get(1).getId();
        UUID done = board.getStages().get(2).getId();
        int countBefore = board.getActivityLog().size();

        board.moveStageBefore(operatorId, done, todo, Instant.now());

        assertEquals(List.of(done, todo, doing), board.getStages().stream().map(Stage::getId).toList());
        assertEquals(countBefore + 1, board.getActivityLog().size());
        assertEquals(operatorId, board.getActivityLog().get(board.getActivityLog().size() - 1).getOperatorId());
    }

    @Test
    void should_rejectRemoveStage_when_onlyOneStageLeft() {
        Board board = Board.create(operatorId, "看板", null, Instant.now());
        UUID doing = board.getStages().get(1).getId();
        UUID done = board.getStages().get(2).getId();
        board.removeStage(operatorId, doing, Instant.now());
        board.removeStage(operatorId, done, Instant.now());
        UUID lastStageId = board.getStages().get(0).getId();

        DomainException exception = assertThrows(DomainException.class,
                () -> board.removeStage(operatorId, lastStageId, Instant.now()));

        assertEquals(ErrorCode.MINIMUM_STAGE, exception.getCode());
        assertEquals("看板至少需要保留一個 Stage", exception.getMessage());
    }

    @Test
    void should_rejectRemoveStage_when_stillHasCards() {
        FakeCardLookupPort port = new FakeCardLookupPort();
        Board board = Board.create(operatorId, "看板", port, Instant.now());
        UUID doingStageId = board.getStages().get(1).getId();
        port.addCard(UUID.randomUUID(), board.getSwimlanes().get(0).getId(), doingStageId);
        port.addCard(UUID.randomUUID(), board.getSwimlanes().get(0).getId(), doingStageId);
        board.refreshCardSummaries();

        DomainException exception = assertThrows(DomainException.class,
                () -> board.removeStage(operatorId, doingStageId, Instant.now()));

        assertEquals(ErrorCode.STAGE_HAS_CARDS, exception.getCode());
        assertTrue(exception.getMessage().contains("2"));
    }

    @Test
    void should_removeStage_when_cardsMovedAwayAfterGuardTriggered() {
        FakeCardLookupPort port = new FakeCardLookupPort();
        Board board = Board.create(operatorId, "看板", port, Instant.now());
        UUID doingStageId = board.getStages().get(1).getId();
        UUID todoStageId = board.getStages().get(0).getId();
        UUID cardId = UUID.randomUUID();
        port.addCard(cardId, board.getSwimlanes().get(0).getId(), doingStageId);
        board.refreshCardSummaries();

        assertThrows(DomainException.class, () -> board.removeStage(operatorId, doingStageId, Instant.now()));
        int countBefore = board.getActivityLog().size();

        port.moveCardToStage(cardId, todoStageId);
        board.refreshCardSummaries();
        board.removeStage(operatorId, doingStageId, Instant.now());

        assertEquals(List.of("待辦", "完成"), board.getStages().stream().map(Stage::getName).toList());
        assertEquals(countBefore + 1, board.getActivityLog().size());
        assertEquals(operatorId, board.getActivityLog().get(board.getActivityLog().size() - 1).getOperatorId());
    }

    @Test
    void should_setStageRole_and_clearPreviousHolder_when_assigningStart() {
        Board board = Board.create(operatorId, "看板", null, Instant.now());
        UUID todo = board.getStages().get(0).getId();
        UUID doing = board.getStages().get(1).getId();
        int countBefore = board.getActivityLog().size();

        board.setStageRole(operatorId, todo, StageRole.START, Instant.now());
        assertEquals(countBefore + 1, board.getActivityLog().size());
        board.setStageRole(operatorId, doing, StageRole.START, Instant.now());

        assertEquals(StageRole.NONE, board.getStages().get(0).getRole());
        assertEquals(StageRole.START, board.getStages().get(1).getRole());
        assertEquals(countBefore + 2, board.getActivityLog().size());
        assertEquals(operatorId, board.getActivityLog().get(board.getActivityLog().size() - 1).getOperatorId());
    }

    /**
     * {@link CardLookupPort} 的測試替身：Card Aggregate（T-03）尚未實作，
     * 用可手動操控的假資料驗證 Board 端依卡片數量做的刪除保護機制。
     */
    private static final class FakeCardLookupPort implements CardLookupPort {

        private final List<CardSummary> cards = new ArrayList<>();

        void addCard(UUID cardId, UUID swimlaneId, UUID stageId) {
            cards.add(new CardSummary(cardId, swimlaneId, stageId));
        }

        void moveCardToStage(UUID cardId, UUID newStageId) {
            cards.removeIf(c -> c.cardId().equals(cardId));
            cards.add(new CardSummary(cardId, findSwimlaneOf(cardId), newStageId));
        }

        private UUID findSwimlaneOf(UUID cardId) {
            return cards.stream().filter(c -> c.cardId().equals(cardId)).findFirst()
                    .map(CardSummary::swimlaneId).orElse(null);
        }

        @Override
        public List<CardSummary> findByBoardId(UUID boardId) {
            return List.copyOf(cards);
        }
    }
}
