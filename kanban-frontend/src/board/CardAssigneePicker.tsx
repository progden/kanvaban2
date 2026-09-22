// s-card-assignee-picker（F02 spec-user-membership.md）：依操作表／驗收條件實作；
// 版面依 AssigneePicker.dc.html（見 .dev/ui-prototype/README.md）。
// 候選清單依 uc-list-card-assignee-candidates 只列該 card 所屬 board 的成員；
// 取消不套用勾選變更、不觸發 uc-set-card-assignees（純前端狀態，儲存才呼叫 API）。
import { useEffect, useState } from 'react';
import { listAssigneeCandidates, setAssignees, type AssigneeCandidate, type CardResponse } from '../api/cardApi';
import { ApiError } from '../api/http';
import { avatarColorFor } from './avatarColor';
import '../pages/BoardListPage.css';
import './Board.css';

interface CardAssigneePickerProps {
  boardId: string;
  card: CardResponse;
  onCancel: () => void;
  onSaved: (card: CardResponse) => void;
}

export function CardAssigneePicker({ boardId, card, onCancel, onSaved }: CardAssigneePickerProps) {
  const [candidates, setCandidates] = useState<AssigneeCandidate[] | null>(null);
  const [selectedIds, setSelectedIds] = useState<string[]>(card.assigneeIds);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [saveError, setSaveError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    let cancelled = false;
    listAssigneeCandidates(boardId)
      .then((result) => {
        if (!cancelled) {
          setCandidates(result);
        }
      })
      .catch((e: unknown) => {
        if (!cancelled) {
          setLoadError(e instanceof ApiError ? e.message : '載入候選清單失敗，請稍後再試');
        }
      });
    return () => {
      cancelled = true;
    };
  }, [boardId]);

  function toggle(userId: string) {
    setSelectedIds((current) =>
      current.includes(userId) ? current.filter((id) => id !== userId) : [...current, userId],
    );
  }

  async function handleSave() {
    setSaving(true);
    try {
      const updated = await setAssignees(card.id, selectedIds);
      onSaved(updated);
    } catch (e) {
      setSaveError(e instanceof ApiError ? e.message : '設定負責人失敗，請稍後再試');
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="dialog-backdrop">
      <div className="dialog-panel">
        <h2>指派負責人</h2>
        <p className="board-dialog__subtitle">{card.title}</p>

        {loadError !== null && (
          <p role="alert" className="form-error">
            {loadError}
          </p>
        )}
        {saveError !== null && (
          <p role="alert" className="form-error">
            {saveError}
          </p>
        )}

        {candidates === null && loadError === null && <p>載入中…</p>}

        {candidates !== null && (
          <ul className="board-assignee-picker__list">
            {candidates.map((candidate) => (
              <li key={candidate.id}>
                <label className="board-assignee-picker__item">
                  <input
                    type="checkbox"
                    checked={selectedIds.includes(candidate.id)}
                    onChange={() => toggle(candidate.id)}
                  />
                  <span className="avatar" style={{ background: avatarColorFor(candidate.id) }}>
                    {candidate.displayName.charAt(0)}
                  </span>
                  <span className="board-assignee-picker__name">{candidate.displayName}</span>
                  <span className="board-assignee-picker__username">{candidate.username}</span>
                </label>
              </li>
            ))}
          </ul>
        )}

        <div className="board-dialog__actions">
          <button type="button" className="btn-secondary" onClick={onCancel}>
            取消
          </button>
          <button type="button" className="btn" disabled={saving} onClick={() => void handleSave()}>
            儲存
          </button>
        </div>
      </div>
    </div>
  );
}
