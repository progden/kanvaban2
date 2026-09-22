// s-swimlane-list ＋ s-swimlane-delete-dialog：依 ui-kanban-basic.md 操作表／驗收條件實作；
// 版面依 CanvasPanel.dc.html／SwimlaneDeleteDialog.dc.html（見 .dev/ui-prototype/README.md）。
// 進入點：F07 s-canvas 選中看板 item 後的操作面板（OQ-17，機制細節待整合 CR 定案）；
// T-14 先以看板 item 標頭的「管理 Swimlane」按鈕開啟本對話框，等 s-canvas 補上選取面板後再整合（decision-log）。
// r-board-owner 才能真的寫入，本畫面對所有能編輯看板的成員顯示，非 Owner 送出時依
// uc-reject-structure-change-by-member 由後端拒絕、顯示訊息（不在前端重複判斷角色）。
import { useState } from 'react';
import {
  addSwimlane,
  moveSwimlane,
  removeSwimlane,
  renameSwimlane,
  type BoardResponse,
} from '../api/boardApi';
import { ApiError } from '../api/http';
import '../pages/BoardListPage.css';
import './Board.css';

interface SwimlanePanelProps {
  board: BoardResponse;
  cardCountBySwimlane: Map<string, number>;
  onCancel: () => void;
  onChanged: () => void;
}

export function SwimlanePanel({ board, cardCountBySwimlane, onCancel, onChanged }: SwimlanePanelProps) {
  const [newName, setNewName] = useState('');
  const [addError, setAddError] = useState<string | null>(null);
  const [renamingId, setRenamingId] = useState<string | null>(null);
  const [renameValue, setRenameValue] = useState('');
  const [rowError, setRowError] = useState<string | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<{ id: string; name: string } | null>(null);
  const [dragId, setDragId] = useState<string | null>(null);

  const swimlanes = [...board.swimlanes].sort((a, b) => a.order - b.order);

  async function handleAdd() {
    try {
      await addSwimlane(board.id, newName);
      setNewName('');
      setAddError(null);
      onChanged();
    } catch (e) {
      setAddError(e instanceof ApiError ? e.message : '新增泳道失敗，請稍後再試');
    }
  }

  async function handleRename(swimlaneId: string) {
    try {
      await renameSwimlane(board.id, swimlaneId, renameValue);
      setRenamingId(null);
      setRowError(null);
      onChanged();
    } catch (e) {
      setRowError(e instanceof ApiError ? e.message : '重新命名失敗，請稍後再試');
    }
  }

  async function handleReorder(swimlaneId: string, beforeId: string | null) {
    try {
      await moveSwimlane(board.id, swimlaneId, beforeId);
      onChanged();
    } catch (e) {
      setRowError(e instanceof ApiError ? e.message : '調整順序失敗，請稍後再試');
    }
  }

  return (
    <div className="dialog-backdrop">
      <div className="dialog-panel board-structure-panel">
        <div className="board-structure-panel__head">
          <h2>管理泳道</h2>
          <button type="button" className="btn-sm" aria-label="關閉面板" onClick={onCancel}>
            關閉
          </button>
        </div>

        {rowError !== null && (
          <p role="alert" className="form-error">
            {rowError}
          </p>
        )}

        <ul className="board-structure-panel__list">
          {swimlanes.map((swimlane) => (
            <li
              key={swimlane.id}
              draggable
              onDragStart={() => setDragId(swimlane.id)}
              onDragOver={(event) => event.preventDefault()}
              onDrop={(event) => {
                event.preventDefault();
                if (dragId !== null && dragId !== swimlane.id) {
                  void handleReorder(dragId, swimlane.id);
                }
                setDragId(null);
              }}
            >
              {renamingId === swimlane.id ? (
                <>
                  <input
                    className="field-input"
                    value={renameValue}
                    onChange={(event) => setRenameValue(event.target.value)}
                  />
                  <button type="button" className="btn-sm" onClick={() => void handleRename(swimlane.id)}>
                    儲存
                  </button>
                </>
              ) : (
                <>
                  <span className="board-structure-panel__name">{swimlane.name}</span>
                  <button
                    type="button"
                    className="btn-sm"
                    onClick={() => {
                      setRenamingId(swimlane.id);
                      setRenameValue(swimlane.name);
                    }}
                  >
                    重新命名
                  </button>
                  <button
                    type="button"
                    className="btn-sm"
                    disabled={swimlanes.length <= 1}
                    onClick={() => setDeleteTarget({ id: swimlane.id, name: swimlane.name })}
                  >
                    刪除
                  </button>
                </>
              )}
            </li>
          ))}
        </ul>

        <div className="board-structure-panel__add">
          <label className="field-label" htmlFor="swimlane-new-name">
            新增泳道
          </label>
          <div className="board-structure-panel__add-row">
            <input
              id="swimlane-new-name"
              className="field-input"
              value={newName}
              onChange={(event) => setNewName(event.target.value)}
            />
            <button type="button" className="btn" onClick={() => void handleAdd()}>
              新增
            </button>
          </div>
          {addError !== null && (
            <p role="alert" className="form-error">
              {addError}
            </p>
          )}
        </div>
      </div>

      {deleteTarget !== null && (
        <div className="dialog-backdrop">
          <div className="dialog-panel">
            <h2>刪除泳道「{deleteTarget.name}」？</h2>
            {(cardCountBySwimlane.get(deleteTarget.id) ?? 0) > 0 && (
              <p className="board-dialog__subtitle">
                這個泳道裡的 {cardCountBySwimlane.get(deleteTarget.id) ?? 0} 張卡片會一起被刪除。
              </p>
            )}
            <SwimlaneDeleteConfirm
              boardId={board.id}
              swimlaneId={deleteTarget.id}
              onCancel={() => setDeleteTarget(null)}
              onDeleted={() => {
                setDeleteTarget(null);
                onChanged();
              }}
            />
          </div>
        </div>
      )}
    </div>
  );
}

function SwimlaneDeleteConfirm({
  boardId,
  swimlaneId,
  onCancel,
  onDeleted,
}: {
  boardId: string;
  swimlaneId: string;
  onCancel: () => void;
  onDeleted: () => void;
}) {
  const [error, setError] = useState<string | null>(null);
  const [deleting, setDeleting] = useState(false);

  async function handleConfirm() {
    setDeleting(true);
    try {
      await removeSwimlane(boardId, swimlaneId);
      onDeleted();
    } catch (e) {
      setError(e instanceof ApiError ? e.message : '刪除泳道失敗，請稍後再試');
    } finally {
      setDeleting(false);
    }
  }

  return (
    <>
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
          刪除泳道
        </button>
      </div>
    </>
  );
}
