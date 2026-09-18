package io.progden.kanban.spring.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.progden.kanban.core.domain.Board;
import io.progden.kanban.core.domain.BoardRole;
import io.progden.kanban.core.domain.Card;
import io.progden.kanban.core.domain.CardPlacement;
import io.progden.kanban.core.domain.CardRepository;
import io.progden.kanban.core.domain.DomainException;
import io.progden.kanban.core.domain.ErrorCode;
import io.progden.kanban.core.domain.User;
import io.progden.kanban.core.domain.UserRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * D-01 修正的整合測試：驗證非該 {@code board} 成員、以及唯讀的 Viewer，不能新增卡片或設定負責人
 * （Review 第 1 輪退回項目）。
 */
@SpringBootTest
class CardApplicationServiceTest {

    @Autowired
    private BoardApplicationService boardApplicationService;

    @Autowired
    private BoardMembershipApplicationService boardMembershipApplicationService;

    @Autowired
    private CardApplicationService cardApplicationService;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private UserRepository userRepository;

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
        Card card = Card.create(ownerId, board.getId(), "卡片 A", new CardPlacement(swimlaneId, stageId));
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
}
