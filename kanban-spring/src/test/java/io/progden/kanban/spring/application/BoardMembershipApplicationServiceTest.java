package io.progden.kanban.spring.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.progden.kanban.core.domain.Board;
import io.progden.kanban.core.domain.BoardRole;
import io.progden.kanban.core.domain.DomainException;
import io.progden.kanban.core.domain.ErrorCode;
import io.progden.kanban.core.domain.User;
import io.progden.kanban.core.domain.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * D-01／D-02 修正的整合測試：非成員不能查詢看板成員名單，且 {@code uc-view-board-list}
 * 依 post 字面排除 Viewer（Review 第 1 輪退回項目）。
 */
@SpringBootTest
class BoardMembershipApplicationServiceTest {

    @Autowired
    private BoardApplicationService boardApplicationService;

    @Autowired
    private BoardMembershipApplicationService boardMembershipApplicationService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void 非成員查詢成員名單時被拒絕() {
        UUID ownerId = UUID.randomUUID();
        Board board = boardApplicationService.createBoard(ownerId, "測試看板");
        UUID nonMemberId = UUID.randomUUID();

        assertThatThrownBy(() -> boardMembershipApplicationService.ensureMember(board.getId(), nonMemberId))
                .isInstanceOf(DomainException.class)
                .extracting(e -> ((DomainException) e).getCode())
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    void 看板列表不包含我只是Viewer的board() {
        UUID ownerId = UUID.randomUUID();
        Board board = boardApplicationService.createBoard(ownerId, "測試看板");
        User viewer = User.create("viewer2", "旁觀者2", "pw", false);
        userRepository.save(viewer);
        boardMembershipApplicationService.inviteMember(board.getId(), ownerId, viewer.getId(), BoardRole.VIEWER);

        assertThat(boardMembershipApplicationService.listBoardsForUser(viewer.getId())).isEmpty();
        assertThat(boardMembershipApplicationService.listBoardsForUser(ownerId))
                .extracting(Board::getId)
                .containsExactly(board.getId());
    }
}
