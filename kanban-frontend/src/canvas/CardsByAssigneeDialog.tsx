// s-cards-by-assignee：依 ui-user-membership.md 操作表／驗收條件實作（F02，跨模組，本任務範圍見
// tasks.md T-19 產出範圍）。依 ui-workload.md「進入與離開」，從 s-workload-dashboard 點擊成員工作量
// 進入，中途放棄回到 s-workload-dashboard；沒有提交流程，故做成可關閉的對話框而非路由頁面。
// 「點擊清單中的卡片開啟 F01 s-card-detail」：s-card-detail 是「看板本體」item（T-14）內部狀態，
// 不是獨立路由，透過呼叫端傳入的 onOpenCard（實際是 BoardContext 的 requestCardDetail，見
// OQ-T-19-fe-workload-04 解除說明）跨 item 通知它開啟指定卡片；本畫面點擊後自行關閉，避免疊出兩層對話框。
import { useEffect, useState } from 'react';
import { listCardsByAssignee, type CardSummary } from '../api/cardApi';
import { ApiError } from '../api/http';
import './CanvasStage.css';

interface CardsByAssigneeDialogProps {
  boardId: string;
  userId: string;
  displayName: string;
  onClose: () => void;
  onOpenCard: (cardId: string) => void;
}

export function CardsByAssigneeDialog({
  boardId,
  userId,
  displayName,
  onClose,
  onOpenCard,
}: CardsByAssigneeDialogProps) {
  const [cards, setCards] = useState<CardSummary[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    listCardsByAssignee(boardId, userId)
      .then((result) => {
        if (!cancelled) {
          setCards(result);
        }
      })
      .catch((e: unknown) => {
        if (!cancelled) {
          setError(e instanceof ApiError ? e.message : '載入卡片清單失敗，請稍後再試');
        }
      });
    return () => {
      cancelled = true;
    };
  }, [boardId, userId]);

  return (
    <div className="dialog-backdrop">
      <div className="dialog-panel">
        <h2>{displayName} 負責的卡片</h2>

        {error !== null && (
          <p role="alert" className="form-error">
            {error}
          </p>
        )}

        {error === null && cards === null && <p>載入中…</p>}
        {error === null && cards !== null && cards.length === 0 && <p>目前沒有負責任何卡片</p>}
        {error === null && cards !== null && cards.length > 0 && (
          <ul data-testid="cards-by-assignee-list" className="cards-by-assignee__list">
            {cards.map((card) => (
              <li key={card.id}>
                <button type="button" className="cards-by-assignee__card" onClick={() => onOpenCard(card.id)}>
                  {card.title}
                </button>
              </li>
            ))}
          </ul>
        )}

        <div className="canvas-dialog__actions">
          <button type="button" className="btn" onClick={onClose}>
            關閉
          </button>
        </div>
      </div>
    </div>
  );
}
