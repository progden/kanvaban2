package io.progden.kanban.spring.application;

import static org.assertj.core.api.Assertions.assertThat;

import io.progden.kanban.core.domain.Board;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * D-05 修正的整合測試：{@code itemIds} 為空或未指定（{@code null}）時，
 * {@code uc-move-items}／{@code uc-remove-items} 的 {@code pre} 真空成立，
 * 批次操作視為 no-op，不可再丟出未攔截的執行期例外（Review 第 3 輪退回項目）。
 */
@SpringBootTest
class CanvasApplicationServiceTest {

    @Autowired
    private BoardApplicationService boardApplicationService;

    @Autowired
    private CanvasApplicationService canvasApplicationService;

    @Test
    void 批次移動時itemIds為空陣列視為no_op() {
        UUID ownerId = UUID.randomUUID();
        Board board = boardApplicationService.createBoard(ownerId, "測試看板");
        canvasApplicationService.initCanvas(board.getId(), ownerId);

        List<?> moved = canvasApplicationService.moveItems(board.getId(), ownerId, List.of(), 10, 10);

        assertThat(moved).isEmpty();
    }

    @Test
    void 批次移動時itemIds為null視為no_op() {
        UUID ownerId = UUID.randomUUID();
        Board board = boardApplicationService.createBoard(ownerId, "測試看板");
        canvasApplicationService.initCanvas(board.getId(), ownerId);

        List<?> moved = canvasApplicationService.moveItems(board.getId(), ownerId, null, 10, 10);

        assertThat(moved).isEmpty();
    }

    @Test
    void 批次移除時itemIds為空陣列視為no_op() {
        UUID ownerId = UUID.randomUUID();
        Board board = boardApplicationService.createBoard(ownerId, "測試看板");
        canvasApplicationService.initCanvas(board.getId(), ownerId);

        canvasApplicationService.removeItems(board.getId(), ownerId, List.of());
    }

    @Test
    void 批次移除時itemIds為null視為no_op() {
        UUID ownerId = UUID.randomUUID();
        Board board = boardApplicationService.createBoard(ownerId, "測試看板");
        canvasApplicationService.initCanvas(board.getId(), ownerId);

        canvasApplicationService.removeItems(board.getId(), ownerId, null);
    }
}
