// uc-place-item 的最小輸入表單：spec-canvas-layout.md 只定義「board」這一個 item.component 值
// （由 uc-init-canvas 自動建立），其餘元件的值待各自模組定案（見 ui-canvas-layout.md 待確認事項）；
// 畫布工具列本身的版面 spec 也未定義（Main.dc.html 標註「⚠️ 規格未定義：畫布工具列本身」），
// 這裡沿用既有對話框樣式，先讓 uc-place-item 這個操作在畫面上可被觸發與測試。
import { useState } from 'react';
import { ApiError } from '../api/http';
import type { ItemAnchor } from '../api/canvasApi';
import './CanvasStage.css';

interface PlaceItemDialogProps {
  onCancel: () => void;
  onSubmit: (input: {
    component: string;
    x: number;
    y: number;
    width: number;
    height: number;
    anchor: ItemAnchor;
  }) => Promise<void>;
}

export function PlaceItemDialog({ onCancel, onSubmit }: PlaceItemDialogProps) {
  const [component, setComponent] = useState('');
  const [width, setWidth] = useState('300');
  const [height, setHeight] = useState('200');
  const [anchor, setAnchor] = useState<ItemAnchor>('canvas');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit() {
    setSubmitting(true);
    setError(null);
    try {
      await onSubmit({ component, x: 0, y: 0, width: Number(width), height: Number(height), anchor });
    } catch (e) {
      setError(e instanceof ApiError ? e.message : '放置元件失敗，請稍後再試');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="dialog-backdrop">
      <div className="dialog-panel">
        <h2>加入元件</h2>

        {error !== null && (
          <p role="alert" className="form-error">
            {error}
          </p>
        )}

        <div className="field">
          <label className="field-label" htmlFor="place-item-component">
            元件識別碼
          </label>
          <input
            id="place-item-component"
            className="field-input"
            value={component}
            onChange={(e) => setComponent(e.target.value)}
          />
        </div>
        <div className="field" style={{ display: 'flex', gap: 12 }}>
          <div style={{ flex: 1 }}>
            <label className="field-label" htmlFor="place-item-width">
              寬
            </label>
            <input
              id="place-item-width"
              type="number"
              className="field-input"
              value={width}
              onChange={(e) => setWidth(e.target.value)}
            />
          </div>
          <div style={{ flex: 1 }}>
            <label className="field-label" htmlFor="place-item-height">
              高
            </label>
            <input
              id="place-item-height"
              type="number"
              className="field-input"
              value={height}
              onChange={(e) => setHeight(e.target.value)}
            />
          </div>
        </div>
        <div className="field">
          <label className="field-label" htmlFor="place-item-anchor">
            錨定方式
          </label>
          <select
            id="place-item-anchor"
            className="field-input"
            value={anchor}
            onChange={(e) => setAnchor(e.target.value as ItemAnchor)}
          >
            <option value="canvas">隨畫布移動</option>
            <option value="screen">固定於畫面</option>
          </select>
        </div>

        <div className="canvas-dialog__actions">
          <button type="button" className="btn-secondary" onClick={onCancel}>
            取消
          </button>
          <button type="button" className="btn" disabled={submitting} onClick={() => void handleSubmit()}>
            加入
          </button>
        </div>
      </div>
    </div>
  );
}
