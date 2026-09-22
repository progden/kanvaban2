package io.progden.kanban.spring.persistence;

import io.progden.kanban.core.domain.Canvas;
import io.progden.kanban.core.domain.CanvasRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/**
 * {@link CanvasRepository} port 的 JPA 實作，負責 {@code Canvas}（domain）與 {@code CanvasJpaEntity}
 * 互轉。{@code Canvas} 的欄位（{@code zoom-min}／{@code zoom-max}）建立後不會再變（見 domain 類別
 * 註解），因此 {@code save} 只在不存在時新增，不處理更新。
 */
@Repository
class CanvasRepositoryAdapter implements CanvasRepository {

    private final CanvasJpaRepository jpaRepository;

    CanvasRepositoryAdapter(CanvasJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(Canvas canvas) {
        if (jpaRepository.existsById(canvas.getId())) {
            return;
        }
        jpaRepository.save(new CanvasJpaEntity(canvas.getId(), canvas.getBoardId(), canvas.getZoomMin(),
                canvas.getZoomMax()));
    }

    @Override
    public Optional<Canvas> findById(UUID canvasId) {
        return jpaRepository.findById(canvasId).map(this::toDomain);
    }

    @Override
    public Optional<Canvas> findByBoardId(UUID boardId) {
        return jpaRepository.findByBoardId(boardId).map(this::toDomain);
    }

    private Canvas toDomain(CanvasJpaEntity entity) {
        return Canvas.reconstruct(entity.getId(), entity.getBoardId(), entity.getZoomMin(), entity.getZoomMax());
    }
}
