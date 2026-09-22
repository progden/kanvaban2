package io.progden.kanban.query;

import io.progden.kanban.spring.persistence.ActivityRecordJpaEntity;
import io.progden.kanban.spring.persistence.BoardJpaRepository;
import io.progden.kanban.spring.persistence.BoardMembershipActivityJpaRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * spec-user-membership.md《檢視看板活動紀錄》Feature（{@code uc-view-board-activity-log}）：把
 * {@code Board} 自己的活動紀錄與 {@code board-membership} 的活動紀錄合併成一份、依時間新到舊排序。
 *
 * <p>這是跨 Aggregate 的讀取投影，不屬於任何一個 Aggregate 的職責，依 design-user-membership.md
 * 第 8 節留給 kanban-spring 的查詢層實作，不放進 kanban-core。
 */
@Service
public class BoardActivityLogQueryService {

    private final BoardJpaRepository boardJpaRepository;
    private final BoardMembershipActivityJpaRepository membershipActivityJpaRepository;

    public BoardActivityLogQueryService(
            BoardJpaRepository boardJpaRepository, BoardMembershipActivityJpaRepository membershipActivityJpaRepository) {
        this.boardJpaRepository = boardJpaRepository;
        this.membershipActivityJpaRepository = membershipActivityJpaRepository;
    }

    public List<BoardActivityLogEntry> viewBoardActivityLog(UUID boardId) {
        List<BoardActivityLogEntry> entries = new ArrayList<>();
        boardJpaRepository.findById(boardId).ifPresent(board -> {
            for (ActivityRecordJpaEntity record : board.getActivityLog()) {
                entries.add(new BoardActivityLogEntry(record.getOperatorId(), record.getAction(), record.getOccurredAt()));
            }
        });
        for (var record : membershipActivityJpaRepository.findByBoardId(boardId)) {
            entries.add(new BoardActivityLogEntry(record.getOperatorId(), record.getAction(), record.getOccurredAt()));
        }
        entries.sort(Comparator.comparing(BoardActivityLogEntry::occurredAt).reversed());
        return entries;
    }
}
