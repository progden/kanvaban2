// s-card-add-dialog：依 ui-kanban-basic.md 操作表／驗收條件實作；版面依 CardAddDialog.dc.html
// （見 .dev/ui-prototype/README.md）。目的 Swimlane／Stage 由進入情境帶入，本畫面不可變更（uc-add-card）。
// OQ-30（ui-authoring-open-questions.md）待確認 uc-add-card／uc-member-add-card 角色關係，
// 本畫面沿用現況不呈現角色差異，成功／失敗行為以 uc-add-card 為準。
import { useState, type FormEvent } from 'react';
import { addCard, type CardResponse } from '../api/cardApi';
import { ApiError } from '../api/http';
import '../pages/BoardListPage.css';
import './Board.css';

interface CardAddDialogProps {
  boardId: string;
  swimlaneId: string;
  swimlaneName: string;
  stageId: string;
  stageName: string;
  onCancel: () => void;
  onAdded: (card: CardResponse) => void;
}

export function CardAddDialog({
  boardId,
  swimlaneId,
  swimlaneName,
  stageId,
  stageName,
  onCancel,
  onAdded,
}: CardAddDialogProps) {
  const [title, setTitle] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitting(true);
    try {
      const card = await addCard(boardId, title, swimlaneId, stageId);
      onAdded(card);
    } catch (e) {
      setError(e instanceof ApiError ? e.message : '新增卡片失敗，請稍後再試');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="dialog-backdrop">
      <div className="dialog-panel">
        <h2>新增卡片</h2>
        <div className="board-add-card-dialog__target">
          <div>
            <div className="field-label">泳道</div>
            <div>{swimlaneName}</div>
          </div>
          <div>
            <div className="field-label">階段</div>
            <div>{stageName}</div>
          </div>
        </div>

        {error !== null && (
          <p role="alert" className="form-error">
            {error}
          </p>
        )}

        <form onSubmit={(event) => void handleSubmit(event)}>
          <div className="field">
            <label className="field-label" htmlFor="card-add-title">
              卡片標題
            </label>
            <input
              id="card-add-title"
              className="field-input"
              type="text"
              value={title}
              onChange={(event) => setTitle(event.target.value)}
            />
          </div>
          <div className="board-dialog__actions">
            <button type="button" className="btn-secondary" onClick={onCancel}>
              取消
            </button>
            <button type="submit" className="btn" disabled={submitting}>
              新增卡片
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
