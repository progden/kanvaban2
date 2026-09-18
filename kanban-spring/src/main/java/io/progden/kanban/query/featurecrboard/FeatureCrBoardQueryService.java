package io.progden.kanban.query.featurecrboard;

import io.progden.kanban.core.domain.DomainException;
import io.progden.kanban.core.domain.ErrorCode;
import io.progden.kanban.core.domain.StageRole;
import io.progden.kanban.spring.persistence.BoardJpaEntity;
import io.progden.kanban.spring.persistence.BoardJpaRepository;
import io.progden.kanban.spring.persistence.CardJpaEntity;
import io.progden.kanban.spring.persistence.CardJpaRepository;
import io.progden.kanban.spring.persistence.StageJpaEntity;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * spec-feature-cr-board.md {@code uc-view-feature-cr-board} 對應的跨 aggregate 讀取投影：
 * 只讀 {@code board} 的 Stage 角色與 {@code card} 的標籤／所在 Stage（Aggregate 標記說明），組成
 * {@link CardLabelProjection} 交給純函式 {@link FeatureCrBoardCalculator} 計算。
 */
@Service
public class FeatureCrBoardQueryService {

    private final BoardJpaRepository boardJpaRepository;
    private final CardJpaRepository cardJpaRepository;

    public FeatureCrBoardQueryService(BoardJpaRepository boardJpaRepository, CardJpaRepository cardJpaRepository) {
        this.boardJpaRepository = boardJpaRepository;
        this.cardJpaRepository = cardJpaRepository;
    }

    @Transactional(readOnly = true)
    public FeatureCrBoardView view(UUID boardId) {
        BoardJpaEntity board = boardJpaRepository.findById(boardId)
                .orElseThrow(() -> new DomainException(ErrorCode.BOARD_NOT_FOUND, "找不到指定的看板"));
        Map<UUID, StageRole> roleByStageId = board.getStages().stream()
                .collect(Collectors.toMap(StageJpaEntity::getId, StageJpaEntity::getRole));

        var projections = cardJpaRepository.findByBoardIdAndDeletedFalse(boardId).stream()
                .map(c -> toProjection(c, roleByStageId))
                .toList();

        return FeatureCrBoardCalculator.calculate(projections);
    }

    private CardLabelProjection toProjection(CardJpaEntity card, Map<UUID, StageRole> roleByStageId) {
        StageRole role = roleByStageId.getOrDefault(card.getStageId(), StageRole.NONE);
        return new CardLabelProjection(card.getId(), card.getTitle(), card.getLabels(), role);
    }
}
