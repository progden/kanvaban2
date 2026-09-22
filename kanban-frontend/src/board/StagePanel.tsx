// s-stage-list ＋ s-stage-delete-dialog：依 ui-kanban-basic.md 操作表／驗收條件實作；
// 版面依 PanelStage.dc.html／StageDeleteDialog.dc.html（見 .dev/ui-prototype/README.md）。
// 進入點同 SwimlanePanel（見該檔註解，OQ-17 機制細節待整合 CR 定案）。
import { useState } from 'react';
import { addStage, moveStage, removeStage, renameStage, setStageRole, type BoardResponse } from '../api/boardApi';
import { ApiError } from '../api/http';
import '../pages/BoardListPage.css';
import './Board.css';

interface StagePanelProps {
  board: BoardResponse;
  cardCountByStage: Map<string, number>;
  onCancel: () => void;
  onChanged: () => void;
}

export function StagePanel({ board, cardCountByStage, onCancel, onChanged }: StagePanelProps) {
  const [newName, setNewName] = useState('');
  const [beforeStageId, setBeforeStageId] = useState<string>('');
  const [addError, setAddError] = useState<string | null>(null);
  const [renamingId, setRenamingId] = useState<string | null>(null);
  const [renameValue, setRenameValue] = useState('');
  const [rowError, setRowError] = useState<string | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<{ id: string; name: string } | null>(null);
  const [dragId, setDragId] = useState<string | null>(null);

  const stages = [...board.stages].sort((a, b) => a.order - b.order);

  async function handleAdd() {
    try {
      await addStage(board.id, newName, beforeStageId.length === 0 ? null : beforeStageId);
      setNewName('');
      setAddError(null);
      onChanged();
    } catch (e) {
      setAddError(e instanceof ApiError ? e.message : '新增階段失敗，請稍後再試');
    }
  }

  async function handleRename(stageId: string) {
    try {
      await renameStage(board.id, stageId, renameValue);
      setRenamingId(null);
      setRowError(null);
      onChanged();
    } catch (e) {
      setRowError(e instanceof ApiError ? e.message : '重新命名失敗，請稍後再試');
    }
  }

  async function handleReorder(stageId: string, beforeId: string | null) {
    try {
      await moveStage(board.id, stageId, beforeId);
      onChanged();
    } catch (e) {
      setRowError(e instanceof ApiError ? e.message : '調整順序失敗，請稍後再試');
    }
  }

  async function handleSetRole(stageId: string, role: string) {
    try {
      await setStageRole(board.id, stageId, role);
      onChanged();
    } catch (e) {
      setRowError(e instanceof ApiError ? e.message : '設定角色失敗，請稍後再試');
    }
  }

  return (
    <div className="dialog-backdrop">
      <div className="dialog-panel board-structure-panel">
        <div className="board-structure-panel__head">
          <h2>管理階段</h2>
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
          {stages.map((stage) => (
            <li
              key={stage.id}
              draggable
              onDragStart={() => setDragId(stage.id)}
              onDragOver={(event) => event.preventDefault()}
              onDrop={(event) => {
                event.preventDefault();
                if (dragId !== null && dragId !== stage.id) {
                  void handleReorder(dragId, stage.id);
                }
                setDragId(null);
              }}
            >
              {renamingId === stage.id ? (
                <>
                  <input
                    className="field-input"
                    value={renameValue}
                    onChange={(event) => setRenameValue(event.target.value)}
                  />
                  <button type="button" className="btn-sm" onClick={() => void handleRename(stage.id)}>
                    儲存
                  </button>
                </>
              ) : (
                <>
                  <span className="board-structure-panel__name">{stage.name}</span>
                  <label className="board-structure-panel__role-label" htmlFor={`stage-role-${stage.id}`}>
                    {stage.name} 的階段角色
                  </label>
                  <select
                    id={`stage-role-${stage.id}`}
                    value={stage.role}
                    onChange={(event) => void handleSetRole(stage.id, event.target.value)}
                  >
                    <option value="NONE">NONE</option>
                    <option value="START">START</option>
                    <option value="DONE">DONE</option>
                  </select>
                  <button
                    type="button"
                    className="btn-sm"
                    disabled={stages.length <= 1}
                    onClick={() => setDeleteTarget({ id: stage.id, name: stage.name })}
                  >
                    刪除
                  </button>
                </>
              )}
            </li>
          ))}
        </ul>

        <div className="board-structure-panel__add">
          <label className="field-label" htmlFor="stage-new-name">
            新增階段
          </label>
          <div className="board-structure-panel__add-row">
            <input
              id="stage-new-name"
              className="field-input"
              value={newName}
              onChange={(event) => setNewName(event.target.value)}
            />
            <label htmlFor="stage-new-position" className="board-structure-panel__role-label">
              插入位置
            </label>
            <select
              id="stage-new-position"
              value={beforeStageId}
              onChange={(event) => setBeforeStageId(event.target.value)}
            >
              <option value="">最後（預設）</option>
              {stages.map((stage) => (
                <option key={stage.id} value={stage.id}>
                  {stage.name} 之前
                </option>
              ))}
            </select>
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
            <h2>刪除階段「{deleteTarget.name}」？</h2>
            <StageDeleteConfirm
              boardId={board.id}
              stageId={deleteTarget.id}
              cardCount={cardCountByStage.get(deleteTarget.id) ?? 0}
              otherStages={stages.filter((s) => s.id !== deleteTarget.id)}
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

function StageDeleteConfirm({
  boardId,
  stageId,
  cardCount,
  otherStages,
  onCancel,
  onDeleted,
}: {
  boardId: string;
  stageId: string;
  cardCount: number;
  otherStages: { id: string; name: string }[];
  onCancel: () => void;
  onDeleted: () => void;
}) {
  const [destinationStageId, setDestinationStageId] = useState(otherStages[0]?.id ?? '');
  const [error, setError] = useState<string | null>(null);
  const [deleting, setDeleting] = useState(false);

  async function handleConfirm() {
    setDeleting(true);
    try {
      await removeStage(boardId, stageId, cardCount > 0 ? destinationStageId : null);
      onDeleted();
    } catch (e) {
      setError(e instanceof ApiError ? e.message : '刪除階段失敗，請稍後再試');
    } finally {
      setDeleting(false);
    }
  }

  return (
    <>
      {cardCount > 0 && (
        <>
          <p className="board-dialog__subtitle">這個階段裡有 {cardCount} 張卡片，選一個階段接收它們。</p>
          <label className="field-label" htmlFor="stage-delete-destination">
            卡片移到
          </label>
          <select
            id="stage-delete-destination"
            className="field-input"
            value={destinationStageId}
            onChange={(event) => setDestinationStageId(event.target.value)}
          >
            {otherStages.map((stage) => (
              <option key={stage.id} value={stage.id}>
                {stage.name}
              </option>
            ))}
          </select>
        </>
      )}
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
          disabled={deleting || (cardCount > 0 && destinationStageId.length === 0)}
          onClick={() => void handleConfirm()}
        >
          刪除階段
        </button>
      </div>
    </>
  );
}
