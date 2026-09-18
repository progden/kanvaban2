package io.progden.kanban.spring.application;

import io.progden.kanban.core.domain.Board;
import io.progden.kanban.core.domain.BoardRepository;
import io.progden.kanban.core.domain.Card;
import io.progden.kanban.core.domain.CardLookupPort;
import io.progden.kanban.core.domain.CardRepository;
import io.progden.kanban.core.domain.DomainException;
import io.progden.kanban.core.domain.ErrorCode;
import io.progden.kanban.core.domain.StageRole;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * spec-kanban-basic.md「Swimlane 管理」「Stage（階段）管理」對應的 application 層。
 *
 * <p>{@code r-board-owner} 權限檢查（僅 Owner 可調整看板結構）依賴 F02 的 {@code BoardMembership}
 * （T-04 尚未實作），本服務目前只要求呼叫端已登入即可操作，尚未強制「僅 Owner」——見
 * implementation-loop OQ-IMPL-15，待 T-04 補上。
 *
 * <p>{@code removeSwimlane}／{@code removeStage} 協調 {@code uc-delete-swimlane}／
 * {@code uc-delete-stage} post 第 2 條「卡片一併刪除／轉移」：先重新整理 {@code Board} 的卡片快取，
 * 若呼叫端已確認（Swimlane）／指定目的 Stage（Stage），先透過 {@link CardRepository} 完成卡片的
 * 刪除／轉移，重新整理快取後再呼叫一次 {@code Board} 的刪除方法（此時卡片數應為 0，可順利完成）；
 * 未確認／未指定目的 Stage 時沿用既有行為，有卡片就丟出 {@code SWIMLANE_HAS_CARDS}／
 * {@code STAGE_HAS_CARDS}（見 design-kanban-basic.md 第 7 節、implementation-loop OQ-IMPL-17）。
 */
@Service
public class BoardApplicationService {

    private final BoardRepository boardRepository;
    private final CardLookupPort cardLookupPort;
    private final CardRepository cardRepository;

    public BoardApplicationService(
            BoardRepository boardRepository, CardLookupPort cardLookupPort, CardRepository cardRepository) {
        this.boardRepository = boardRepository;
        this.cardLookupPort = cardLookupPort;
        this.cardRepository = cardRepository;
    }

    public Board createBoard(UUID operatorId, String name) {
        Board board = Board.create(operatorId, name, cardLookupPort);
        boardRepository.save(board);
        return board;
    }

    public Board getBoard(UUID boardId) {
        return loadBoard(boardId);
    }

    public Board addSwimlane(UUID boardId, UUID operatorId, String name) {
        Board board = loadBoard(boardId);
        board.addSwimlane(operatorId, name);
        boardRepository.save(board);
        return board;
    }

    public Board renameSwimlane(UUID boardId, UUID operatorId, UUID swimlaneId, String newName) {
        Board board = loadBoard(boardId);
        board.renameSwimlane(operatorId, swimlaneId, newName);
        boardRepository.save(board);
        return board;
    }

    public Board moveSwimlane(UUID boardId, UUID operatorId, UUID swimlaneId, UUID beforeSwimlaneId) {
        Board board = loadBoard(boardId);
        board.moveSwimlaneBefore(operatorId, swimlaneId, beforeSwimlaneId);
        boardRepository.save(board);
        return board;
    }

    public Board removeSwimlane(UUID boardId, UUID operatorId, UUID swimlaneId, boolean confirmed) {
        Board board = loadBoard(boardId);
        board.refreshCardSummaries();
        if (confirmed) {
            for (Card card : cardRepository.findActiveBySwimlaneId(swimlaneId)) {
                card.delete(operatorId);
                cardRepository.save(card);
            }
            board.refreshCardSummaries();
        }
        board.removeSwimlane(operatorId, swimlaneId);
        boardRepository.save(board);
        return board;
    }

    public Board addStage(UUID boardId, UUID operatorId, String name, UUID beforeStageId) {
        Board board = loadBoard(boardId);
        board.addStage(operatorId, name, beforeStageId);
        boardRepository.save(board);
        return board;
    }

    public Board renameStage(UUID boardId, UUID operatorId, UUID stageId, String newName) {
        Board board = loadBoard(boardId);
        board.renameStage(operatorId, stageId, newName);
        boardRepository.save(board);
        return board;
    }

    public Board moveStage(UUID boardId, UUID operatorId, UUID stageId, UUID beforeStageId) {
        Board board = loadBoard(boardId);
        board.moveStageBefore(operatorId, stageId, beforeStageId);
        boardRepository.save(board);
        return board;
    }

    public Board removeStage(UUID boardId, UUID operatorId, UUID stageId, UUID destinationStageId) {
        Board board = loadBoard(boardId);
        board.refreshCardSummaries();
        if (destinationStageId != null) {
            for (Card card : cardRepository.findActiveByStageId(stageId)) {
                card.moveToStage(operatorId, destinationStageId);
                cardRepository.save(card);
            }
            board.refreshCardSummaries();
        }
        board.removeStage(operatorId, stageId);
        boardRepository.save(board);
        return board;
    }

    public Board setStageRole(UUID boardId, UUID operatorId, UUID stageId, StageRole role) {
        Board board = loadBoard(boardId);
        board.setStageRole(operatorId, stageId, role);
        boardRepository.save(board);
        return board;
    }

    private Board loadBoard(UUID boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new DomainException(ErrorCode.BOARD_NOT_FOUND, "找不到指定的看板"));
    }
}
