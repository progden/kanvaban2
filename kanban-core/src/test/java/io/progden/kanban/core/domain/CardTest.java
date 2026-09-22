package io.progden.kanban.core.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@code Card}（含內部 {@code Comment}／{@code StageTransition}）不變條件單元測試，
 * 對應 spec-kanban-basic.md「Card（卡片）編輯」Feature。
 *
 * <p>所有寫入方法都改為接受呼叫端傳入的 {@code now}（見 T-05 board-clock 的重構，design-
 * board-clock.md 第 2 節：{@code Card} 沒有自己的時鐘，時間一律由呼叫端先讀 {@code Board} 的
 * {@code newEventTime} 算出後傳入），這裡一律用 {@link Instant#now()} 模擬這個已經算好的時間。
 */
class CardTest {

    private final UUID operatorId = UUID.randomUUID();
    private final UUID boardId = UUID.randomUUID();
    private final UUID swimlaneId = UUID.randomUUID();
    private final UUID stageId = UUID.randomUUID();

    @Test
    void should_createCardAtPlacement_when_creating() {
        Card card = Card.create(operatorId, boardId, "設計登入頁面", new CardPlacement(swimlaneId, stageId),
                Instant.now());

        assertEquals("設計登入頁面", card.getTitle());
        assertEquals(swimlaneId, card.getSwimlaneId());
        assertEquals(stageId, card.getStageId());
        assertFalse(card.isDeleted());
        assertEquals(1, card.getActivityLog().size());
        assertEquals(operatorId, card.getActivityLog().get(0).getOperatorId());
    }

    @Test
    void should_rejectCreate_when_titleIsBlank() {
        DomainException exception = assertThrows(DomainException.class,
                () -> Card.create(operatorId, boardId, " ", new CardPlacement(swimlaneId, stageId), Instant.now()));

        assertEquals(ErrorCode.EMPTY_CARD_TITLE, exception.getCode());
    }

    @Test
    void should_updateFieldsAndRecordActivity_when_editing() {
        Card card = Card.create(operatorId, boardId, "設計登入頁面", new CardPlacement(swimlaneId, stageId),
                Instant.now());
        int countBefore = card.getActivityLog().size();

        card.edit(operatorId, new CardDetails("設計符合品牌風格的登入頁面",
                LocalDate.of(2026, 9, 20), List.of("UI", "前端")), Instant.now());

        assertEquals("設計符合品牌風格的登入頁面", card.getDescription());
        assertEquals(LocalDate.of(2026, 9, 20), card.getDueDate());
        assertEquals(List.of("UI", "前端"), card.getLabels());
        assertEquals(countBefore + 1, card.getActivityLog().size());
    }

    @Test
    void should_updateSwimlane_when_movingToAnotherSwimlane() {
        Card card = Card.create(operatorId, boardId, "設計登入頁面", new CardPlacement(swimlaneId, stageId),
                Instant.now());
        UUID otherSwimlaneId = UUID.randomUUID();

        card.moveToSwimlane(operatorId, otherSwimlaneId, Instant.now());

        assertEquals(otherSwimlaneId, card.getSwimlaneId());
        assertEquals(stageId, card.getStageId());
    }

    @Test
    void should_recordStageTransition_when_movingToAnotherStage() {
        Card card = Card.create(operatorId, boardId, "設計登入頁面", new CardPlacement(swimlaneId, stageId),
                Instant.now());
        UUID otherStageId = UUID.randomUUID();

        card.moveToStage(operatorId, otherStageId, Instant.now());

        assertEquals(otherStageId, card.getStageId());
        assertEquals(1, card.getStageTransitions().size());
        StageTransition transition = card.getStageTransitions().get(0);
        assertEquals(operatorId, transition.getOperatorId());
        assertEquals(stageId, transition.getFromStageId());
        assertEquals(otherStageId, transition.getToStageId());
    }

    @Test
    void should_addComment_when_contentIsNotBlank() {
        Card card = Card.create(operatorId, boardId, "設計登入頁面", new CardPlacement(swimlaneId, stageId),
                Instant.now());
        UUID authorId = UUID.randomUUID();

        Comment comment = card.addComment(authorId, "已完成初稿，請協助審閱", Instant.now());

        assertEquals(1, card.getComments().size());
        assertEquals("已完成初稿，請協助審閱", comment.getContent());
        assertEquals(authorId, comment.getAuthorId());
    }

    @Test
    void should_rejectAddComment_when_contentIsBlank() {
        Card card = Card.create(operatorId, boardId, "設計登入頁面", new CardPlacement(swimlaneId, stageId),
                Instant.now());

        DomainException exception = assertThrows(DomainException.class,
                () -> card.addComment(operatorId, " ", Instant.now()));

        assertEquals(ErrorCode.EMPTY_COMMENT_CONTENT, exception.getCode());
    }

    @Test
    void should_markDeletedAndRecordActivity_when_deleting() {
        Card card = Card.create(operatorId, boardId, "設計登入頁面", new CardPlacement(swimlaneId, stageId),
                Instant.now());
        int countBefore = card.getActivityLog().size();

        card.delete(operatorId, Instant.now());

        assertTrue(card.isDeleted());
        assertEquals(countBefore + 1, card.getActivityLog().size());
    }

    @Test
    void should_updateAssigneesAndRecordActivity_when_assignToChangesAssignees() {
        Card card = Card.create(operatorId, boardId, "設計登入頁面", new CardPlacement(swimlaneId, stageId),
                Instant.now());
        UUID assigneeId = UUID.randomUUID();
        int countBefore = card.getActivityLog().size();

        boolean changed = card.assignTo(operatorId, List.of(assigneeId), "將卡片負責人設定為 雅婷", Instant.now());

        assertTrue(changed);
        assertEquals(List.of(assigneeId), card.getAssigneeIds());
        assertEquals(countBefore + 1, card.getActivityLog().size());
    }

    @Test
    void should_notRecordActivity_when_assignToWithSameAssignees() {
        Card card = Card.create(operatorId, boardId, "設計登入頁面", new CardPlacement(swimlaneId, stageId),
                Instant.now());
        UUID assigneeId = UUID.randomUUID();
        card.assignTo(operatorId, List.of(assigneeId), "將卡片負責人設定為 雅婷", Instant.now());
        int countBefore = card.getActivityLog().size();

        boolean changed = card.assignTo(operatorId, List.of(assigneeId), "將卡片負責人設定為 雅婷", Instant.now());

        assertFalse(changed);
        assertEquals(countBefore, card.getActivityLog().size());
    }

    @Test
    void should_dedupeAssignees_when_assignToHasDuplicateIds() {
        Card card = Card.create(operatorId, boardId, "設計登入頁面", new CardPlacement(swimlaneId, stageId),
                Instant.now());
        UUID assigneeId = UUID.randomUUID();

        card.assignTo(operatorId, List.of(assigneeId, assigneeId), "將卡片負責人設定為 雅婷", Instant.now());

        assertEquals(List.of(assigneeId), card.getAssigneeIds());
    }

    @Test
    void should_removeAssigneeWithoutActivity_when_unassignMember() {
        Card card = Card.create(operatorId, boardId, "設計登入頁面", new CardPlacement(swimlaneId, stageId),
                Instant.now());
        UUID assigneeId = UUID.randomUUID();
        card.assignTo(operatorId, List.of(assigneeId), "將卡片負責人設定為 雅婷", Instant.now());
        int countBefore = card.getActivityLog().size();

        card.unassignMember(assigneeId);

        assertTrue(card.getAssigneeIds().isEmpty());
        assertEquals(countBefore, card.getActivityLog().size());
    }
}
