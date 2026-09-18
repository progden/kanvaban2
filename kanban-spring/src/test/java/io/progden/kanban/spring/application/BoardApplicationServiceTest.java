package io.progden.kanban.spring.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.progden.kanban.core.domain.Board;
import io.progden.kanban.core.domain.BoardRepository;
import io.progden.kanban.core.domain.Card;
import io.progden.kanban.core.domain.CardPlacement;
import io.progden.kanban.core.domain.CardRepository;
import io.progden.kanban.core.domain.DomainException;
import io.progden.kanban.core.domain.ErrorCode;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * D-01 修正的整合測試：驗證 {@code removeSwimlane}／{@code removeStage} 在檢查失敗時，
 * 卡片異動與 Board 結構異動要整個回滾，不留下半套資料（Review 第 1 輪退回項目）。
 */
@SpringBootTest
class BoardApplicationServiceTest {

    @Autowired
    private BoardApplicationService boardApplicationService;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private CardRepository cardRepository;

    @Test
    void 只剩一個Swimlane時確認刪除仍拒絕且卡片不被刪除() {
        UUID operatorId = UUID.randomUUID();
        Board board = boardApplicationService.createBoard(operatorId, "測試看板");
        UUID swimlaneId = board.getSwimlanes().get(0).getId();
        UUID stageId = board.getStages().get(0).getId();

        Card card = Card.create(operatorId, board.getId(), "卡片 A", new CardPlacement(swimlaneId, stageId));
        cardRepository.save(card);

        assertThatThrownBy(() -> boardApplicationService.removeSwimlane(board.getId(), operatorId, swimlaneId, true))
                .isInstanceOf(DomainException.class)
                .extracting(e -> ((DomainException) e).getCode())
                .isEqualTo(ErrorCode.MINIMUM_SWIMLANE);

        Card reloaded = cardRepository.findById(card.getId()).orElseThrow();
        assertThat(reloaded.isDeleted()).isFalse();
    }

    @Test
    void 目的Stage不屬於該board時拒絕且卡片不被轉移() {
        UUID operatorId = UUID.randomUUID();
        Board boardA = boardApplicationService.createBoard(operatorId, "看板 A");
        Board boardB = boardApplicationService.createBoard(operatorId, "看板 B");

        UUID swimlaneId = boardA.getSwimlanes().get(0).getId();
        UUID sourceStageId = boardA.getStages().get(0).getId();
        UUID foreignStageId = boardB.getStages().get(0).getId();

        Card card = Card.create(operatorId, boardA.getId(), "卡片 A", new CardPlacement(swimlaneId, sourceStageId));
        cardRepository.save(card);

        assertThatThrownBy(() -> boardApplicationService.removeStage(
                boardA.getId(), operatorId, sourceStageId, foreignStageId))
                .isInstanceOf(DomainException.class)
                .extracting(e -> ((DomainException) e).getCode())
                .isEqualTo(ErrorCode.STAGE_NOT_FOUND);

        Card reloaded = cardRepository.findById(card.getId()).orElseThrow();
        assertThat(reloaded.isDeleted()).isFalse();
        assertThat(reloaded.getStageId()).isEqualTo(sourceStageId);
        assertThat(reloaded.getStageTransitions()).isEmpty();

        Board reloadedBoardA = boardRepository.findById(boardA.getId()).orElseThrow();
        assertThat(reloadedBoardA.getStages()).extracting(s -> s.getId()).contains(sourceStageId);
    }
}
