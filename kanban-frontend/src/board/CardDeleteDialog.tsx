// s-card-delete-dialog：依 ui-kanban-basic.md 操作表／驗收條件實作；版面依 CardDeleteDialog.dc.html
// （見 .dev/ui-prototype/README.md），對話框沿用 index.css 的 .dialog-backdrop／.dialog-panel。
import { useState } from 'react';
import { deleteCard } from '../api/cardApi';
import { ApiError } from '../api/http';
import '../pages/BoardListPage.css';

interface CardDeleteDialogProps {
  cardId: string;
  cardTitle: string;
  onCancel: () => void;
  onDeleted: () => void;
}

export function CardDeleteDialog({ cardId, cardTitle, onCancel, onDeleted }: CardDeleteDialogProps) {
  const [error, setError] = useState<string | null>(null);
  const [deleting, setDeleting] = useState(false);

  async function handleConfirm() {
    setDeleting(true);
    try {
      await deleteCard(cardId);
      onDeleted();
    } catch (e) {
      setError(e instanceof ApiError ? e.message : '刪除卡片失敗，請稍後再試');
    } finally {
      setDeleting(false);
    }
  }

  return (
    <div className="dialog-backdrop">
      <div className="dialog-panel">
        <h2>刪除卡片「{cardTitle}」？</h2>
        <p className="board-dialog__subtitle">刪除後無法復原。</p>

        {error !== null && (
          <p role="alert" className="form-error">
            {error}
          </p>
        )}

        <div className="board-dialog__actions">
          <button type="button" className="btn-secondary" onClick={onCancel}>
            取消
          </button>
          <button
            type="button"
            className="btn board-delete-dialog__confirm"
            disabled={deleting}
            onClick={() => void handleConfirm()}
          >
            刪除卡片
          </button>
        </div>
      </div>
    </div>
  );
}
