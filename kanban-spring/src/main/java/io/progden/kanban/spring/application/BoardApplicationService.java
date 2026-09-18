package io.progden.kanban.spring.application;

import io.progden.kanban.core.domain.Board;
import io.progden.kanban.core.domain.BoardRepository;
import io.progden.kanban.core.domain.Card;
import io.progden.kanban.core.domain.CardLookupPort;
import io.progden.kanban.core.domain.CardRepository;
import io.progden.kanban.core.domain.DomainException;
import io.progden.kanban.core.domain.ErrorCode;
import io.progden.kanban.core.domain.StageRole;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * spec-kanban-basic.md「Swimlane 管理」「Stage（階段）管理」＋
 * spec-user-membership.md「Board 建立與成員邀請」post 第 2 條、「Board 權限管理」「Board 存取權限」
 * 對應的 application 層。
 *
 * <p>{@code r-board-owner} 權限檢查（僅 Owner 可調整看板結構／刪除看板）由
 * {@link BoardMembershipApplicationService#ensureOwner} 提供，implementation-loop T-04 交接事項 1：
 * 九個結構調整端點（{@code addSwimlane}…{@code setStageRole}）與 {@code deleteBoard} 都先檢查
 * Owner 身分才動作，非 Owner 一律回 {@code FORBIDDEN}（見 uc-reject-structure-change-by-member）。
 *
 * <p>{@code createBoard} 在同一次交易內建立 Owner 的 {@code board-membership}
 * （implementation-loop T-04 交接事項 2：uc-create-board post 第 2 條，不可以只成功一半）。
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

    private static final String STRUCTURE_FORBIDDEN_MESSAGE = "只有 Owner 可以調整看板結構";

    private final BoardRepository boardRepository;
    private final CardLookupPort cardLookupPort;
    private final CardRepository cardRepository;
    private final BoardMembershipApplicationService boardMembershipApplicationService;

    public BoardApplicationService(BoardRepository boardRepository, CardLookupPort cardLookupPort,
            CardRepository cardRepository, BoardMembershipApplicationService boardMembershipApplicationService) {
        this.boardRepository = boardRepository;
        this.cardLookupPort = cardLookupPort;
        this.cardRepository = cardRepository;
        this.boardMembershipApplicationService = boardMembershipApplicationService;
    }

    @Transactional
    public Board createBoard(UUID operatorId, String name) {
        Board board = Board.create(operatorId, name, cardLookupPort);
        boardRepository.save(board);
        boardMembershipApplicationService.createOwnerMembership(board.getId(), operatorId);
        return board;
    }

    public Board getBoard(UUID boardId, UUID operatorId) {
        Board board = loadBoard(boardId);
        boardMembershipApplicationService.ensureMember(boardId, operatorId);
        return board;
    }

    public List<Board> listBoards(UUID operatorId) {
        return boardMembershipApplicationService.listBoardsForUser(operatorId);
    }

    @Transactional
    public void deleteBoard(UUID boardId, UUID operatorId) {
        loadBoard(boardId);
        boardMembershipApplicationService.ensureOwner(boardId, operatorId, "只有 Owner 可以刪除看板");
        for (Card card : cardRepository.findActiveByBoardId(boardId)) {
            card.delete(operatorId);
            cardRepository.save(card);
        }
        boardRepository.deleteById(boardId);
    }

    public Board addSwimlane(UUID boardId, UUID operatorId, String name) {
        Board board = loadBoard(boardId);
        boardMembershipApplicationService.ensureOwner(boardId, operatorId, STRUCTURE_FORBIDDEN_MESSAGE);
        board.addSwimlane(operatorId, name);
        boardRepository.save(board);
        return board;
    }

    public Board renameSwimlane(UUID boardId, UUID operatorId, UUID swimlaneId, String newName) {
        Board board = loadBoard(boardId);
        boardMembershipApplicationService.ensureOwner(boardId, operatorId, STRUCTURE_FORBIDDEN_MESSAGE);
        board.renameSwimlane(operatorId, swimlaneId, newName);
        boardRepository.save(board);
        return board;
    }

    public Board moveSwimlane(UUID boardId, UUID operatorId, UUID swimlaneId, UUID beforeSwimlaneId) {
        Board board = loadBoard(boardId);
        boardMembershipApplicationService.ensureOwner(boardId, operatorId, STRUCTURE_FORBIDDEN_MESSAGE);
        board.moveSwimlaneBefore(operatorId, swimlaneId, beforeSwimlaneId);
        boardRepository.save(board);
        return board;
    }

    @Transactional
    public Board removeSwimlane(UUID boardId, UUID operatorId, UUID swimlaneId, boolean confirmed) {
        Board board = loadBoard(boardId);
        boardMembershipApplicationService.ensureOwner(boardId, operatorId, STRUCTURE_FORBIDDEN_MESSAGE);
        board.refreshCardSummaries();
        board.ensureSwimlaneRemovable(swimlaneId);
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
        boardMembershipApplicationService.ensureOwner(boardId, operatorId, STRUCTURE_FORBIDDEN_MESSAGE);
        board.addStage(operatorId, name, beforeStageId);
        boardRepository.save(board);
        return board;
    }

    public Board renameStage(UUID boardId, UUID operatorId, UUID stageId, String newName) {
        Board board = loadBoard(boardId);
        boardMembershipApplicationService.ensureOwner(boardId, operatorId, STRUCTURE_FORBIDDEN_MESSAGE);
        board.renameStage(operatorId, stageId, newName);
        boardRepository.save(board);
        return board;
    }

    public Board moveStage(UUID boardId, UUID operatorId, UUID stageId, UUID beforeStageId) {
        Board board = loadBoard(boardId);
        boardMembershipApplicationService.ensureOwner(boardId, operatorId, STRUCTURE_FORBIDDEN_MESSAGE);
        board.moveStageBefore(operatorId, stageId, beforeStageId);
        boardRepository.save(board);
        return board;
    }

    @Transactional
    public Board removeStage(UUID boardId, UUID operatorId, UUID stageId, UUID destinationStageId) {
        Board board = loadBoard(boardId);
        boardMembershipApplicationService.ensureOwner(boardId, operatorId, STRUCTURE_FORBIDDEN_MESSAGE);
        board.refreshCardSummaries();
        board.ensureStageRemovable(stageId);
        if (destinationStageId != null) {
            board.ensureValidDestinationStage(stageId, destinationStageId);
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
        boardMembershipApplicationService.ensureOwner(boardId, operatorId, STRUCTURE_FORBIDDEN_MESSAGE);
        board.setStageRole(operatorId, stageId, role);
        boardRepository.save(board);
        return board;
    }

    private Board loadBoard(UUID boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new DomainException(ErrorCode.BOARD_NOT_FOUND, "找不到指定的看板"));
    }
}
