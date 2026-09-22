// ui-canvas-layout.md 操作表：移除元件／批次移除元件「需確認？」皆為「是」。
import { useState } from 'react';
import { ApiError } from '../api/http';
import './CanvasStage.css';

interface RemoveItemsDialogProps {
  count: number;
  onCancel: () => void;
  onConfirm: () => Promise<void>;
}

export function RemoveItemsDialog({ count, onCancel, onConfirm }: RemoveItemsDialogProps) {
  const [error, setError] = useState<string | null>(null);
  const [removing, setRemoving] = useState(false);

  async function handleConfirm() {
    setRemoving(true);
    setError(null);
    try {
      await onConfirm();
    } catch (e) {
      setError(e instanceof ApiError ? e.message : '移除失敗，請稍後再試');
    } finally {
      setRemoving(false);
    }
  }

  return (
    <div className="dialog-backdrop">
      <div className="dialog-panel">
        <h2>{count > 1 ? `移除這 ${count} 個元件？` : '移除這個元件？'}</h2>

        {error !== null && (
          <p role="alert" className="form-error">
            {error}
          </p>
        )}

        <div className="canvas-dialog__actions">
          <button type="button" className="btn-secondary" onClick={onCancel}>
            取消
          </button>
          <button type="button" className="btn" disabled={removing} onClick={() => void handleConfirm()}>
            移除
          </button>
        </div>
      </div>
    </div>
  );
}
