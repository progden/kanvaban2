package io.progden.kanban.spring.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.progden.kanban.core.domain.Board;
import io.progden.kanban.core.domain.BoardRepository;
import io.progden.kanban.core.domain.Card;
import io.progden.kanban.core.domain.CardRepository;
import io.progden.kanban.core.domain.DomainException;
import io.progden.kanban.core.domain.ErrorCode;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * D-01 修正的整合測試：卡片寫入在 domain 驗證失敗時，Board 的 {@code lastEventAt} 基準
 * 不可以被推進並存檔，否則後續 {@code uc-guard-clock-monotonicity} 的判斷會引用一個不存在的
 * 「最後一筆事件」（Review 第 1 輪退回項目）。
 */
@SpringBootTest
class CardApplicationServiceTest {

    @Autowired
    private CardApplicationService cardApplicationService;

    @Autowired
    private BoardApplicationService boardApplicationService;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private CardRepository cardRepository;

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
}
