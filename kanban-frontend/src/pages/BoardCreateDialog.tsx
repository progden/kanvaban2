// s-board-create-dialog：依 ui-user-membership.md 操作表／驗收條件實作；版面依 BoardCreateDialog.dc.html
// （見 .dev/ui-prototype/README.md「檔案與 Screen ID」），對話框沿用 index.css 的 .dialog-backdrop／.dialog-panel。
import { useState, type FormEvent } from 'react';
import { createBoard, type BoardResponse } from '../api/boardApi';
import { ApiError } from '../api/http';
import './BoardListPage.css';

interface BoardCreateDialogProps {
  onCancel: () => void;
  onCreated: (board: BoardResponse) => void;
}

export function BoardCreateDialog({ onCancel, onCreated }: BoardCreateDialogProps) {
  const [name, setName] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitting(true);
    try {
      const board = await createBoard(name);
      onCreated(board);
    } catch (e) {
      setError(e instanceof ApiError ? e.message : '建立看板失敗，請稍後再試');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="dialog-backdrop">
      <div className="dialog-panel">
        <h2>建立看板</h2>
        <p className="board-dialog__subtitle">你會成為這個看板的 Owner。</p>

        {error !== null && (
          <p role="alert" className="form-error">
            {error}
          </p>
        )}

        <form onSubmit={(event) => void handleSubmit(event)}>
          <div className="field">
            <label className="field-label" htmlFor="board-create-name">
              看板名稱
            </label>
            <input
              id="board-create-name"
              className="field-input"
              type="text"
              value={name}
              onChange={(event) => setName(event.target.value)}
            />
          </div>
          <div className="board-dialog__actions">
            <button type="button" className="btn-secondary" onClick={onCancel}>
              取消
            </button>
            <button type="submit" className="btn" disabled={submitting}>
              建立看板
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
