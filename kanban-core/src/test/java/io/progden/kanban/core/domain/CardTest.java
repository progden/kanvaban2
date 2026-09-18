package io.progden.kanban.core.domain;

import org.junit.jupiter.api.Test;

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
 */
class CardTest {

    private final UUID operatorId = UUID.randomUUID();
    private final UUID boardId = UUID.randomUUID();
    private final UUID swimlaneId = UUID.randomUUID();
    private final UUID stageId = UUID.randomUUID();

    @Test
    void should_createCardAtPlacement_when_creating() {
        Card card = Card.create(operatorId, boardId, "設計登入頁面", new CardPlacement(swimlaneId, stageId));

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
                () -> Card.create(operatorId, boardId, " ", new CardPlacement(swimlaneId, stageId)));

        assertEquals(ErrorCode.EMPTY_CARD_TITLE, exception.getCode());
    }

    @Test
    void should_updateFieldsAndRecordActivity_when_editing() {
        Card card = Card.create(operatorId, boardId, "設計登入頁面", new CardPlacement(swimlaneId, stageId));
        int countBefore = card.getActivityLog().size();

        card.edit(operatorId, new CardDetails("設計符合品牌風格的登入頁面",
                LocalDate.of(2026, 9, 20), List.of("UI", "前端")));

        assertEquals("設計符合品牌風格的登入頁面", card.getDescription());
        assertEquals(LocalDate.of(2026, 9, 20), card.getDueDate());
        assertEquals(List.of("UI", "前端"), card.getLabels());
        assertEquals(countBefore + 1, card.getActivityLog().size());
    }

    @Test
    void should_updateSwimlane_when_movingToAnotherSwimlane() {
        Card card = Card.create(operatorId, boardId, "設計登入頁面", new CardPlacement(swimlaneId, stageId));
        UUID otherSwimlaneId = UUID.randomUUID();

        card.moveToSwimlane(operatorId, otherSwimlaneId);

        assertEquals(otherSwimlaneId, card.getSwimlaneId());
        assertEquals(stageId, card.getStageId());
    }

    @Test
    void should_recordStageTransition_when_movingToAnotherStage() {
        Card card = Card.create(operatorId, boardId, "設計登入頁面", new CardPlacement(swimlaneId, stageId));
        UUID otherStageId = UUID.randomUUID();

        card.moveToStage(operatorId, otherStageId);

        assertEquals(otherStageId, card.getStageId());
        assertEquals(1, card.getStageTransitions().size());
        StageTransition transition = card.getStageTransitions().get(0);
        assertEquals(operatorId, transition.getOperatorId());
        assertEquals(stageId, transition.getFromStageId());
        assertEquals(otherStageId, transition.getToStageId());
    }

    @Test
    void should_addComment_when_contentIsNotBlank() {
        Card card = Card.create(operatorId, boardId, "設計登入頁面", new CardPlacement(swimlaneId, stageId));
        UUID authorId = UUID.randomUUID();

        Comment comment = card.addComment(authorId, "已完成初稿，請協助審閱");

        assertEquals(1, card.getComments().size());
        assertEquals("已完成初稿，請協助審閱", comment.getContent());
        assertEquals(authorId, comment.getAuthorId());
    }

    @Test
    void should_rejectAddComment_when_contentIsBlank() {
        Card card = Card.create(operatorId, boardId, "設計登入頁面", new CardPlacement(swimlaneId, stageId));

        DomainException exception = assertThrows(DomainException.class,
                () -> card.addComment(operatorId, " "));

        assertEquals(ErrorCode.EMPTY_COMMENT_CONTENT, exception.getCode());
    }

    @Test
    void should_markDeletedAndRecordActivity_when_deleting() {
        Card card = Card.create(operatorId, boardId, "設計登入頁面", new CardPlacement(swimlaneId, stageId));
        int countBefore = card.getActivityLog().size();

        card.delete(operatorId);

        assertTrue(card.isDeleted());
        assertEquals(countBefore + 1, card.getActivityLog().size());
    }
}
