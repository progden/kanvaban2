package io.progden.kanban.spring.application;

import io.progden.kanban.core.domain.Board;
import io.progden.kanban.core.domain.BoardRepository;
import io.progden.kanban.core.domain.Card;
import io.progden.kanban.core.domain.CardLookupPort;
import io.progden.kanban.core.domain.CardRepository;
import io.progden.kanban.core.domain.DomainException;
import io.progden.kanban.core.domain.ErrorCode;
import io.progden.kanban.core.domain.StageRole;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * spec-kanban-basic.md「Swimlane 管理」「Stage（階段）管理」對應的 application 層。
 *
 * <p>{@code r-board-owner} 權限檢查（僅 Owner 可調整看板結構）依賴 F02 的 {@code BoardMembership}
 * （T-04 尚未實作），本服務目前只要求呼叫端已登入即可操作，尚未強制「僅 Owner」——見
 * implementation-loop OQ-IMPL-15，待 T-04 補上。
 *
 * <p>{@code removeSwimlane}／{@code removeStage} 協調 {@code uc-delete-swimlane}／
 * {@code uc-delete-stage} post 第 2 條「卡片一併刪除／轉移」：先呼叫 {@code Board} 的
 * {@code ensureSwimlaneRemovable}／{@code ensureStageRemovable}（連同目的 Stage 的
 * {@code ensureValidDestinationStage}）完成「該 swimlane／stage 屬於這個 board、數量下限、
 * 目的 Stage 合法」的檢查，全部通過才透過 {@link CardRepository} 動卡片（刪除／轉移），最後才呼叫
 * {@code Board} 的刪除方法；未確認／未指定目的 Stage 時沿用既有行為，有卡片就丟出
 * {@code SWIMLANE_HAS_CARDS}／{@code STAGE_HAS_CARDS}（見 design-kanban-basic.md 第 7 節、
 * implementation-loop OQ-IMPL-17）。兩個方法都標 {@code @Transactional}：檢查通過後若卡片異動或
 * 最終刪除任一步失敗，整個協調流程（含已異動的卡片）一起回滾，不留下半套資料。
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
        Board board = Board.create(operatorId, name, cardLookupPort, Instant.now());
        boardRepository.save(board);
        return board;
    }

    public Board getBoard(UUID boardId) {
        return loadBoard(boardId);
    }

    public Board addSwimlane(UUID boardId, UUID operatorId, String name) {
        Board board = loadBoard(boardId);
        board.addSwimlane(operatorId, name, Instant.now());
        boardRepository.save(board);
        return board;
    }

    public Board renameSwimlane(UUID boardId, UUID operatorId, UUID swimlaneId, String newName) {
        Board board = loadBoard(boardId);
        board.renameSwimlane(operatorId, swimlaneId, newName, Instant.now());
        boardRepository.save(board);
        return board;
    }

    public Board moveSwimlane(UUID boardId, UUID operatorId, UUID swimlaneId, UUID beforeSwimlaneId) {
        Board board = loadBoard(boardId);
        board.moveSwimlaneBefore(operatorId, swimlaneId, beforeSwimlaneId, Instant.now());
        boardRepository.save(board);
        return board;
    }

    @Transactional
    public Board removeSwimlane(UUID boardId, UUID operatorId, UUID swimlaneId, boolean confirmed) {
        Board board = loadBoard(boardId);
        board.refreshCardSummaries();
        board.ensureSwimlaneRemovable(swimlaneId);
        if (confirmed) {
            for (Card card : cardRepository.findActiveBySwimlaneId(swimlaneId)) {
                Instant now = board.newEventTime(Instant.now());
                card.delete(operatorId, now);
                cardRepository.save(card);
            }
            board.refreshCardSummaries();
        }
        board.removeSwimlane(operatorId, swimlaneId, Instant.now());
        boardRepository.save(board);
        return board;
    }

    public Board addStage(UUID boardId, UUID operatorId, String name, UUID beforeStageId) {
        Board board = loadBoard(boardId);
        board.addStage(operatorId, name, beforeStageId, Instant.now());
        boardRepository.save(board);
        return board;
    }

    public Board renameStage(UUID boardId, UUID operatorId, UUID stageId, String newName) {
        Board board = loadBoard(boardId);
        board.renameStage(operatorId, stageId, newName, Instant.now());
        boardRepository.save(board);
        return board;
    }

    public Board moveStage(UUID boardId, UUID operatorId, UUID stageId, UUID beforeStageId) {
        Board board = loadBoard(boardId);
        board.moveStageBefore(operatorId, stageId, beforeStageId, Instant.now());
        boardRepository.save(board);
        return board;
    }

    @Transactional
    public Board removeStage(UUID boardId, UUID operatorId, UUID stageId, UUID destinationStageId) {
        Board board = loadBoard(boardId);
        board.refreshCardSummaries();
        board.ensureStageRemovable(stageId);
        if (destinationStageId != null) {
            board.ensureValidDestinationStage(stageId, destinationStageId);
            for (Card card : cardRepository.findActiveByStageId(stageId)) {
                Instant now = board.newEventTime(Instant.now());
                card.moveToStage(operatorId, destinationStageId, now);
                cardRepository.save(card);
            }
            board.refreshCardSummaries();
        }
        board.removeStage(operatorId, stageId, Instant.now());
        boardRepository.save(board);
        return board;
    }

    public Board setStageRole(UUID boardId, UUID operatorId, UUID stageId, StageRole role) {
        Board board = loadBoard(boardId);
        board.setStageRole(operatorId, stageId, role, Instant.now());
        boardRepository.save(board);
        return board;
    }

    /**
     * {@code uc-adjust-board-clock}：{@code r-board-owner} 權限檢查暫以 {@code board.createdBy} 判斷
     * （F02 {@code BoardMembership} 尚未實作，見 OQ-T-05-be-board-clock-01，比照既有 OQ-IMPL-15 的
     * 權限延後模式）。
     */
    public Board adjustClock(UUID boardId, UUID operatorId, Instant newTime) {
        Board board = loadBoard(boardId);
        requireOwner(board, operatorId);
        board.adjustClock(operatorId, newTime, Instant.now());
        boardRepository.save(board);
        return board;
    }

    public Board pauseClock(UUID boardId, UUID operatorId) {
        Board board = loadBoard(boardId);
        requireOwner(board, operatorId);
        board.pauseClock(operatorId, Instant.now());
        boardRepository.save(board);
        return board;
    }

    public Board resumeClock(UUID boardId, UUID operatorId) {
        Board board = loadBoard(boardId);
        requireOwner(board, operatorId);
        board.resumeClock(operatorId, Instant.now());
        boardRepository.save(board);
        return board;
    }

    private void requireOwner(Board board, UUID operatorId) {
        if (!board.getCreatedBy().equals(operatorId)) {
            throw new DomainException(ErrorCode.NOT_BOARD_OWNER, "只有 Owner 可以調整看板時間");
        }
    }

    private Board loadBoard(UUID boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new DomainException(ErrorCode.BOARD_NOT_FOUND, "找不到指定的看板"));
    }
}
