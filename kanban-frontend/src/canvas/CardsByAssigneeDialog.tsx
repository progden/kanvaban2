// s-cards-by-assignee：依 ui-user-membership.md 操作表／驗收條件實作（F02，跨模組，本任務範圍見
// tasks.md T-19 產出範圍）。依 ui-workload.md「進入與離開」，從 s-workload-dashboard 點擊成員工作量
// 進入，中途放棄回到 s-workload-dashboard；沒有提交流程，故做成可關閉的對話框而非路由頁面。
// 「點擊清單中的卡片開啟 F01 s-card-detail」：s-card-detail 所屬的 T-14-fe-board-item 尚未合併，
// 本畫面沒有可導向的目的地，卡片列先做成不可點擊的純顯示列，見交接摘要待確認事項。
import { useEffect, useState } from 'react';
import { listCardsByAssignee, type CardSummary } from '../api/cardApi';
import { ApiError } from '../api/http';
import './CanvasStage.css';

interface CardsByAssigneeDialogProps {
  boardId: string;
  userId: string;
  displayName: string;
  onClose: () => void;
}

export function CardsByAssigneeDialog({ boardId, userId, displayName, onClose }: CardsByAssigneeDialogProps) {
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
              <li key={card.id}>{card.title}</li>
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
