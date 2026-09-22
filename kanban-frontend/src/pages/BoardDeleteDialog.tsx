// s-board-delete-dialog：依 ui-user-membership.md 操作表／驗收條件實作；版面依 BoardDeleteDialog.dc.html
// （見 .dev/ui-prototype/README.md「檔案與 Screen ID」），對話框沿用 index.css 的 .dialog-backdrop／.dialog-panel。
// 卡片數透過 T-12 新增的 GET /api/boards/{boardId}/card-count 取得（見 boardApi.ts 註解）；
// 泳道數／階段數直接用列表已經取得的 board.swimlanes／board.stages 長度，不用再打一次 API。
import { useEffect, useState } from 'react';
import { countActiveCards, deleteBoard, type BoardResponse } from '../api/boardApi';
import { ApiError } from '../api/http';
import './BoardListPage.css';

interface BoardDeleteDialogProps {
  board: BoardResponse;
  onCancel: () => void;
  onDeleted: (boardId: string) => void;
}

export function BoardDeleteDialog({ board, onCancel, onDeleted }: BoardDeleteDialogProps) {
  const [cardCount, setCardCount] = useState<number | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [deleteError, setDeleteError] = useState<string | null>(null);
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    let cancelled = false;
    countActiveCards(board.id)
      .then((count) => {
        if (!cancelled) {
          setCardCount(count);
        }
      })
      .catch((e: unknown) => {
        if (!cancelled) {
          setLoadError(e instanceof ApiError ? e.message : '載入看板資料失敗，請稍後再試');
        }
      });
    return () => {
      cancelled = true;
    };
  }, [board.id]);

  async function handleConfirm() {
    setDeleting(true);
    try {
      await deleteBoard(board.id);
      onDeleted(board.id);
    } catch (e) {
      setDeleteError(e instanceof ApiError ? e.message : '刪除看板失敗，請稍後再試');
    } finally {
      setDeleting(false);
    }
  }

  const loaded = cardCount !== null;

  return (
    <div className="dialog-backdrop">
      <div className="dialog-panel">
        <h2>刪除看板「{board.name}」？</h2>
        <p className="board-dialog__subtitle">底下的東西會一起被刪除：</p>

        {loadError !== null && (
          <p role="alert" className="form-error">
            {loadError}
          </p>
        )}
        {deleteError !== null && (
          <p role="alert" className="form-error">
            {deleteError}
          </p>
        )}

        {!loaded && loadError === null && <p>載入中…</p>}

        {loaded && (
          <div className="board-delete-dialog__counts">
            <div className="board-delete-dialog__count">
              <div className="board-delete-dialog__count-value">{board.swimlanes.length}</div>
              <div className="board-delete-dialog__count-label">泳道</div>
            </div>
            <div className="board-delete-dialog__count">
              <div className="board-delete-dialog__count-value">{board.stages.length}</div>
              <div className="board-delete-dialog__count-label">階段</div>
            </div>
            <div className="board-delete-dialog__count">
              <div className="board-delete-dialog__count-value">{cardCount}</div>
              <div className="board-delete-dialog__count-label">卡片</div>
            </div>
          </div>
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
            刪除看板
          </button>
        </div>
      </div>
    </div>
  );
}
