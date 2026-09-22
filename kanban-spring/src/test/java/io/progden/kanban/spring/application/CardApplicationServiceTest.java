package io.progden.kanban.spring.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.progden.kanban.core.domain.Board;
import io.progden.kanban.core.domain.BoardRepository;
import io.progden.kanban.core.domain.BoardRole;
import io.progden.kanban.core.domain.Card;
import io.progden.kanban.core.domain.CardPlacement;
import io.progden.kanban.core.domain.CardRepository;
import io.progden.kanban.core.domain.DomainException;
import io.progden.kanban.core.domain.ErrorCode;
import io.progden.kanban.core.domain.User;
import io.progden.kanban.core.domain.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 兩個 D-01 修正的整合測試（Review 第 1 輪退回項目）：
 *
 * <p>T-05-be-board-clock：卡片寫入在 domain 驗證失敗時，Board 的 {@code lastEventAt} 基準
 * 不可以被推進並存檔，否則後續 {@code uc-guard-clock-monotonicity} 的判斷會引用一個不存在的
 * 「最後一筆事件」。
 *
 * <p>T-04-be-board-membership：驗證非該 {@code board} 成員、以及唯讀的 Viewer，不能新增卡片或
 * 設定負責人。
 */
@SpringBootTest
class CardApplicationServiceTest {

    @Autowired
    private CardApplicationService cardApplicationService;

    @Autowired
    private BoardApplicationService boardApplicationService;

    @Autowired
    private BoardMembershipApplicationService boardMembershipApplicationService;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void 留言內容為空白被拒絕時board的最後事件基準不推進() {
        UUID operatorId = UUID.randomUUID();
        Board board = boardApplicationService.createBoard(operatorId, "測試看板");
        UUID swimlaneId = board.getSwimlanes().get(0).getId();
        UUID stageId = board.getStages().get(0).getId();

        Card card = cardApplicationService.addCard(board.getId(), operatorId, "卡片 A", swimlaneId, stageId);

        Instant lastEventAtBeforeFailedComment =
                boardRepository.findById(board.getId()).orElseThrow().getClockSnapshot().lastEventAt();

        assertThatThrownBy(() -> cardApplicationService.addComment(card.getId(), operatorId, "   "))
                .isInstanceOf(DomainException.class)
                .extracting(e -> ((DomainException) e).getCode())
                .isEqualTo(ErrorCode.EMPTY_COMMENT_CONTENT);

        Instant lastEventAtAfterFailedComment =
                boardRepository.findById(board.getId()).orElseThrow().getClockSnapshot().lastEventAt();
        assertThat(lastEventAtAfterFailedComment).isEqualTo(lastEventAtBeforeFailedComment);

        Card reloadedCard = cardRepository.findById(card.getId()).orElseThrow();
        assertThat(reloadedCard.getComments()).isEmpty();

        Card secondCard = cardApplicationService.addCard(board.getId(), operatorId, "卡片 B", swimlaneId, stageId);
        assertThat(secondCard).isNotNull();
    }

    @Test
    void 非成員新增卡片時被拒絕且不建立卡片() {
        UUID ownerId = UUID.randomUUID();
        Board board = boardApplicationService.createBoard(ownerId, "測試看板");
        UUID swimlaneId = board.getSwimlanes().get(0).getId();
        UUID stageId = board.getStages().get(0).getId();
        UUID nonMemberId = UUID.randomUUID();

        assertThatThrownBy(() ->
                cardApplicationService.addCard(board.getId(), nonMemberId, "卡片 A", swimlaneId, stageId))
                .isInstanceOf(DomainException.class)
                .extracting(e -> ((DomainException) e).getCode())
                .isEqualTo(ErrorCode.FORBIDDEN);

        assertThat(cardRepository.findActiveByBoardId(board.getId())).isEmpty();
    }

    @Test
    void Viewer新增卡片時被拒絕且不建立卡片() {
        UUID ownerId = UUID.randomUUID();
        Board board = boardApplicationService.createBoard(ownerId, "測試看板");
        UUID swimlaneId = board.getSwimlanes().get(0).getId();
        UUID stageId = board.getStages().get(0).getId();
        User viewer = User.create("viewer1", "旁觀者", "pw", false);
        userRepository.save(viewer);
        boardMembershipApplicationService.inviteMember(board.getId(), ownerId, viewer.getId(), BoardRole.VIEWER);

        assertThatThrownBy(() ->
                cardApplicationService.addCard(board.getId(), viewer.getId(), "卡片 A", swimlaneId, stageId))
                .isInstanceOf(DomainException.class)
                .extracting(e -> ((DomainException) e).getCode())
                .isEqualTo(ErrorCode.FORBIDDEN);

        assertThat(cardRepository.findActiveByBoardId(board.getId())).isEmpty();
    }

    @Test
    void 非成員設定負責人時被拒絕且負責人不變() {
        UUID ownerId = UUID.randomUUID();
        Board board = boardApplicationService.createBoard(ownerId, "測試看板");
        UUID swimlaneId = board.getSwimlanes().get(0).getId();
        UUID stageId = board.getStages().get(0).getId();
        Card card = Card.create(ownerId, board.getId(), "卡片 A", new CardPlacement(swimlaneId, stageId), Instant.now());
        cardRepository.save(card);
        UUID nonMemberId = UUID.randomUUID();

        assertThatThrownBy(() ->
                cardApplicationService.setAssignees(card.getId(), nonMemberId, List.of(ownerId)))
                .isInstanceOf(DomainException.class)
                .extracting(e -> ((DomainException) e).getCode())
                .isEqualTo(ErrorCode.FORBIDDEN);

        Card reloaded = cardRepository.findById(card.getId()).orElseThrow();
        assertThat(reloaded.getAssigneeIds()).isEmpty();
    }

    @Test
    void 列出看板卡片時只回傳該看板未刪除的卡片() {
        UUID ownerId = UUID.randomUUID();
        Board board = boardApplicationService.createBoard(ownerId, "測試看板");
        UUID swimlaneId = board.getSwimlanes().get(0).getId();
        UUID stageId = board.getStages().get(0).getId();
        Card keptCard = cardApplicationService.addCard(board.getId(), ownerId, "保留的卡片", swimlaneId, stageId);
        Card deletedCard = cardApplicationService.addCard(board.getId(), ownerId, "已刪除的卡片", swimlaneId, stageId);
        cardApplicationService.deleteCard(deletedCard.getId(), ownerId);

        Board otherBoard = boardApplicationService.createBoard(ownerId, "另一個看板");
        cardApplicationService.addCard(otherBoard.getId(), ownerId, "另一看板的卡片",
                otherBoard.getSwimlanes().get(0).getId(), otherBoard.getStages().get(0).getId());

        List<Card> cards = cardApplicationService.listCardsForBoard(board.getId(), ownerId);

        assertThat(cards).extracting(Card::getId).containsExactly(keptCard.getId());
    }

    @Test
    void 非成員列出看板卡片時被拒絕() {
        UUID ownerId = UUID.randomUUID();
        Board board = boardApplicationService.createBoard(ownerId, "測試看板");
        UUID nonMemberId = UUID.randomUUID();

        assertThatThrownBy(() -> cardApplicationService.listCardsForBoard(board.getId(), nonMemberId))
                .isInstanceOf(DomainException.class)
                .extracting(e -> ((DomainException) e).getCode())
                .isEqualTo(ErrorCode.FORBIDDEN);
    }
}
