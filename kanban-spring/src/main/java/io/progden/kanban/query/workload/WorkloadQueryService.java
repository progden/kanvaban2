package io.progden.kanban.query.workload;

import io.progden.kanban.core.domain.Board;
import io.progden.kanban.core.domain.BoardMembership;
import io.progden.kanban.core.domain.BoardRepository;
import io.progden.kanban.core.domain.DomainException;
import io.progden.kanban.core.domain.ErrorCode;
import io.progden.kanban.core.domain.Stage;
import io.progden.kanban.core.domain.StageRole;
import io.progden.kanban.spring.application.BoardMembershipApplicationService;
import io.progden.kanban.spring.persistence.CardJpaEntity;
import io.progden.kanban.spring.persistence.CardJpaRepository;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@code uc-view-workload}（spec-workload.md）對應的跨 aggregate 讀取投影：只讀 {@code board} 的
 * Stage 角色設定、{@code board-membership} 的成員清單、{@code card} 的目前狀態，交給
 * {@link WorkloadCalculator} 純函式計算（Aggregate 標記說明）。
 */
@Service
public class WorkloadQueryService {

    private final BoardRepository boardRepository;
    private final CardJpaRepository cardJpaRepository;
    private final BoardMembershipApplicationService boardMembershipApplicationService;

    public WorkloadQueryService(BoardRepository boardRepository, CardJpaRepository cardJpaRepository,
            BoardMembershipApplicationService boardMembershipApplicationService) {
        this.boardRepository = boardRepository;
        this.cardJpaRepository = cardJpaRepository;
        this.boardMembershipApplicationService = boardMembershipApplicationService;
    }

    @Transactional(readOnly = true)
    public WorkloadView viewWorkload(UUID boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new DomainException(ErrorCode.BOARD_NOT_FOUND, "找不到指定的看板"));
        Map<UUID, StageRole> roleByStageId = board.getStages().stream()
                .collect(Collectors.toMap(Stage::getId, Stage::getRole));

        var cards = cardJpaRepository.findByBoardIdAndDeletedFalse(boardId).stream()
                .map(c -> new CardWorkloadSource(c.getId(), c.getStageId(), c.getAssigneeIds()))
                .toList();

        var memberUserIds = boardMembershipApplicationService.listMembers(boardId).stream()
                .map(BoardMembership::getUserId)
                .toList();

        return WorkloadCalculator.calculate(cards, memberUserIds, roleByStageId);
    }
}
