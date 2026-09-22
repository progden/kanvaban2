// s-board：看板本體，掛載為 F07 s-canvas 上 item.component === 'board' 的內容
// （見 canvas/itemComponentRegistry.tsx、spec-canvas-layout.md「待釐清」；uc-init-canvas 建立此
// item 時固定用 component 值 "board"，見 CanvasApplicationService.initCanvas）。
// 依 ui-kanban-basic.md 操作表／驗收條件實作；版面依 Main.dc.html（Swimlane × Stage 交會格）。
// 「拖曳看板成員頭像到卡片追加負責人」（uc-assign-card-owner-by-drag）：拖放來源在「看板工作量」item
// （T-19-fe-workload），協定抽在 canvas/cardAssigneeDrag.ts 共用模組，本檔只需在卡片節點接上
// acceptsCardAssigneeDrop／handleCardAssigneeDrop（見 OQ-T-19-fe-workload-03 解除說明）。
// 另外透過 BoardContext 的 requestedCardId 接住「依負責人查看卡片」item（T-19）點卡片要求開啟
// s-card-detail 的跨 item 通知（見 OQ-T-19-fe-workload-04 解除說明）。
import { Fragment, useEffect, useState } from 'react';
import { getBoard, type BoardResponse } from '../api/boardApi';
import {
  listAssigneeCandidates,
  listCardsForBoard,
  moveCardStage,
  moveCardSwimlane,
  type AssigneeCandidate,
  type CardResponse,
} from '../api/cardApi';
import { ApiError } from '../api/http';
import { acceptsCardAssigneeDrop, handleCardAssigneeDrop } from '../canvas/cardAssigneeDrag';
import { registerItemComponent, type ItemContentProps } from '../canvas/itemComponentRegistry';
import { avatarColorFor } from './avatarColor';
import './Board.css';
import { useBoardContext } from './BoardContext';
import { CardAddDialog } from './CardAddDialog';
import { CardDeleteDialog } from './CardDeleteDialog';
import { CardDetailDialog } from './CardDetailDialog';
import { SwimlanePanel } from './SwimlanePanel';
import { StagePanel } from './StagePanel';

function BoardItemContent(_props: ItemContentProps) {
  const { boardId, canEdit, requestedCardId, clearRequestedCardDetail, notifyCardsChanged } = useBoardContext();
  const [board, setBoard] = useState<BoardResponse | null>(null);
  const [cards, setCards] = useState<CardResponse[] | null>(null);
  const [candidates, setCandidates] = useState<AssigneeCandidate[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [swimlanePanelOpen, setSwimlanePanelOpen] = useState(false);
  const [stagePanelOpen, setStagePanelOpen] = useState(false);
  const [addCardTarget, setAddCardTarget] = useState<{ swimlaneId: string; stageId: string } | null>(null);
  const [detailCardId, setDetailCardId] = useState<string | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<{ id: string; title: string } | null>(null);

  function reload() {
    Promise.all([getBoard(boardId), listCardsForBoard(boardId), listAssigneeCandidates(boardId)])
      .then(([loadedBoard, loadedCards, loadedCandidates]) => {
        setBoard(loadedBoard);
        setCards(loadedCards);
        setCandidates(loadedCandidates);
        // 讓其他依賴卡片資料的 item（工作量、WIP、活動紀錄……）知道要重新抓一次，見 BoardContext.tsx
        // cardsVersion 的說明。reload() 本身也是初次載入用的，這裡多通知一次是無害的多打一次 API。
        notifyCardsChanged();
      })
      .catch((e: unknown) => {
        setError(e instanceof ApiError ? e.message : '載入看板內容失敗，請稍後再試');
      });
  }

  useEffect(() => {
    reload();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [boardId]);

  useEffect(() => {
    if (requestedCardId === null || cards === null) {
      return;
    }
    if (cards.some((c) => c.id === requestedCardId)) {
      setDetailCardId(requestedCardId);
    }
    clearRequestedCardDetail();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [requestedCardId, cards]);

  async function handleDropCard(card: CardResponse, targetSwimlaneId: string, targetStageId: string) {
    try {
      let updated = card;
      if (updated.swimlaneId !== targetSwimlaneId) {
        updated = await moveCardSwimlane(updated.id, targetSwimlaneId);
      }
      if (updated.stageId !== targetStageId) {
        updated = await moveCardStage(updated.id, targetStageId);
      }
      reload();
    } catch (e) {
      setError(e instanceof ApiError ? e.message : '移動卡片失敗，請稍後再試');
    }
  }

  if (board === null || cards === null) {
    return (
      <div className="board-item">
        {error !== null && (
          <p role="alert" className="form-error">
            {error}
          </p>
        )}
        {error === null && <p>載入中…</p>}
      </div>
    );
  }

  const swimlanes = [...board.swimlanes].sort((a, b) => a.order - b.order);
  const stages = [...board.stages].sort((a, b) => a.order - b.order);
  const cardCountBySwimlane = new Map<string, number>();
  const cardCountByStage = new Map<string, number>();
  for (const card of cards) {
    cardCountBySwimlane.set(card.swimlaneId, (cardCountBySwimlane.get(card.swimlaneId) ?? 0) + 1);
    cardCountByStage.set(card.stageId, (cardCountByStage.get(card.stageId) ?? 0) + 1);
  }

  const detailCard = detailCardId === null ? null : (cards.find((c) => c.id === detailCardId) ?? null);
  const swimlaneNameById = new Map(swimlanes.map((s) => [s.id, s.name]));
  const stageNameById = new Map(stages.map((s) => [s.id, s.name]));
  const displayNameById = new Map(candidates.map((c) => [c.id, c.displayName]));

  return (
    <div className="board-item">
      {canEdit && (
        <div className="board-item__toolbar">
          <button type="button" className="btn-sm" onClick={() => setSwimlanePanelOpen(true)}>
            管理 Swimlane
          </button>
          <button type="button" className="btn-sm" onClick={() => setStagePanelOpen(true)}>
            管理 Stage
          </button>
        </div>
      )}

      {error !== null && (
        <p role="alert" className="form-error">
          {error}
        </p>
      )}

      <div
        className="board-grid"
        style={{ gridTemplateColumns: `120px repeat(${stages.length}, minmax(140px, 1fr))` }}
      >
        <div className="board-grid__corner" />
        {stages.map((stage) => (
          <div key={stage.id} className="board-grid__stage-header">
            <span className="board-grid__stage-name" title={stage.name}>
              {stage.name}
            </span>
            {stage.role !== 'NONE' && <span className="board-grid__stage-role">{stage.role}</span>}
          </div>
        ))}

        {swimlanes.map((swimlane) => (
          <Fragment key={swimlane.id}>
            <div className="board-grid__swimlane-label">{swimlane.name}</div>
            {stages.map((stage) => {
              const cellCards = cards.filter(
                (c) => c.swimlaneId === swimlane.id && c.stageId === stage.id,
              );
              return (
                <div
                  key={`${swimlane.id}-${stage.id}`}
                  className="board-grid__cell"
                  onDragOver={(event) => event.preventDefault()}
                  onDrop={(event) => {
                    event.preventDefault();
                    const cardId = event.dataTransfer.getData('text/plain');
                    const card = cards.find((c) => c.id === cardId);
                    if (card !== undefined && canEdit) {
                      void handleDropCard(card, swimlane.id, stage.id);
                    }
                  }}
                >
                  {cellCards.map((card) => (
                    <div
                      key={card.id}
                      className="board-card"
                      draggable={canEdit}
                      onDragStart={(event) => event.dataTransfer.setData('text/plain', card.id)}
                      onDragOver={(event) => {
                        if (canEdit && acceptsCardAssigneeDrop(event)) {
                          event.preventDefault();
                        }
                      }}
                      onDrop={(event) => {
                        if (canEdit && acceptsCardAssigneeDrop(event)) {
                          event.preventDefault();
                          event.stopPropagation();
                          void handleCardAssigneeDrop(event, card.id)
                            .then(reload)
                            .catch((e: unknown) => {
                              setError(e instanceof ApiError ? e.message : '追加負責人失敗，請稍後再試');
                            });
                        }
                      }}
                      onClick={() => setDetailCardId(card.id)}
                    >
                      <div className="board-card__title">{card.title}</div>
                      <div className="board-card__meta">
                        {card.dueDate !== null && <span className="board-card__due">{card.dueDate}</span>}
                        <span className="board-card__avatars">
                          {card.assigneeIds.map((userId) => (
                            <span
                              key={userId}
                              className="avatar"
                              style={{ background: avatarColorFor(userId) }}
                              title={displayNameById.get(userId) ?? userId}
                            >
                              {(displayNameById.get(userId) ?? userId).charAt(0)}
                            </span>
                          ))}
                        </span>
                      </div>
                      {canEdit && (
                        <button
                          type="button"
                          className="board-card__delete"
                          aria-label={`刪除卡片「${card.title}」`}
                          onClick={(event) => {
                            event.stopPropagation();
                            setDeleteTarget({ id: card.id, title: card.title });
                          }}
                        >
                          ×
                        </button>
                      )}
                    </div>
                  ))}
                  {canEdit && (
                    <button
                      type="button"
                      className="board-grid__add-card"
                      onClick={() => setAddCardTarget({ swimlaneId: swimlane.id, stageId: stage.id })}
                    >
                      ＋ 新增卡片
                    </button>
                  )}
                </div>
              );
            })}
          </Fragment>
        ))}
      </div>

      {swimlanePanelOpen && (
        <SwimlanePanel
          board={board}
          cardCountBySwimlane={cardCountBySwimlane}
          onCancel={() => setSwimlanePanelOpen(false)}
          onChanged={reload}
        />
      )}
      {stagePanelOpen && (
        <StagePanel
          board={board}
          cardCountByStage={cardCountByStage}
          onCancel={() => setStagePanelOpen(false)}
          onChanged={reload}
        />
      )}
      {addCardTarget !== null && (
        <CardAddDialog
          boardId={boardId}
          swimlaneId={addCardTarget.swimlaneId}
          swimlaneName={swimlaneNameById.get(addCardTarget.swimlaneId) ?? ''}
          stageId={addCardTarget.stageId}
          stageName={stageNameById.get(addCardTarget.stageId) ?? ''}
          onCancel={() => setAddCardTarget(null)}
          onAdded={() => {
            setAddCardTarget(null);
            reload();
          }}
        />
      )}
      {detailCard !== null && (
        <CardDetailDialog
          boardId={boardId}
          cardId={detailCard.id}
          swimlaneName={swimlaneNameById.get(detailCard.swimlaneId) ?? ''}
          stageName={stageNameById.get(detailCard.stageId) ?? ''}
          onClose={() => setDetailCardId(null)}
          onChanged={reload}
        />
      )}
      {deleteTarget !== null && (
        <CardDeleteDialog
          cardId={deleteTarget.id}
          cardTitle={deleteTarget.title}
          onCancel={() => setDeleteTarget(null)}
          onDeleted={() => {
            setDeleteTarget(null);
            reload();
          }}
        />
      )}
    </div>
  );
}

registerItemComponent('board', BoardItemContent, '看板本體');
