// uc-place-item 的最小輸入表單：spec-canvas-layout.md 只定義「board」這一個 item.component 值
// （由 uc-init-canvas 自動建立），其餘元件的值待各自模組定案（見 ui-canvas-layout.md 待確認事項）；
// 畫布工具列本身的版面 spec 也未定義（Main.dc.html 標註「⚠️ 規格未定義：畫布工具列本身」），
// 這裡沿用既有對話框樣式，先讓 uc-place-item 這個操作在畫面上可被觸發與測試。
// 元件識別碼改用下拉選單而非自由輸入：能加入畫布的元件就是 itemComponentRegistry 裡已經掛了
// 內容的那些（一個固定、有限的集合），手打容易打錯字；"board" 由 uc-init-canvas 自動建立、
// 排除在外，不讓使用者手動再加一個。
import { useState } from 'react';
import { ApiError } from '../api/http';
import type { ItemAnchor } from '../api/canvasApi';
import { listRegisteredComponents } from './itemComponentRegistry';
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
    movable: boolean;
    resizable: boolean;
    removable: boolean;
  }) => Promise<void>;
}

export function PlaceItemDialog({ onCancel, onSubmit }: PlaceItemDialogProps) {
  // 每次開對話框才重新讀 registry：registerItemComponent 是散落在各模組的 side-effect import，
  // 若在模組頂層算好這份清單（曾經這樣寫過），會依 import 順序而定、可能拿到還沒註冊完的半成品清單。
  const [componentOptions] = useState(() => listRegisteredComponents(['board']));
  const [component, setComponent] = useState('');
  const [x, setX] = useState('0');
  const [y, setY] = useState('0');
  const [width, setWidth] = useState('300');
  const [height, setHeight] = useState('200');
  const [anchor, setAnchor] = useState<ItemAnchor>('canvas');
  const [movable, setMovable] = useState(true);
  const [resizable, setResizable] = useState(true);
  const [removable, setRemovable] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit() {
    setSubmitting(true);
    setError(null);
    try {
      await onSubmit({
        component,
        x: Number(x),
        y: Number(y),
        width: Number(width),
        height: Number(height),
        anchor,
        movable,
        resizable,
        removable,
      });
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
          <select
            id="place-item-component"
            className="field-input"
            value={component}
            onChange={(e) => setComponent(e.target.value)}
          >
            <option value="" disabled>
              請選擇元件
            </option>
            {componentOptions.map((option) => (
              <option key={option.component} value={option.component}>
                {option.label}
              </option>
            ))}
          </select>
        </div>
        <div className="field" style={{ display: 'flex', gap: 12 }}>
          <div style={{ flex: 1 }}>
            <label className="field-label" htmlFor="place-item-x">
              X
            </label>
            <input
              id="place-item-x"
              type="number"
              className="field-input"
              value={x}
              onChange={(e) => setX(e.target.value)}
            />
          </div>
          <div style={{ flex: 1 }}>
            <label className="field-label" htmlFor="place-item-y">
              Y
            </label>
            <input
              id="place-item-y"
              type="number"
              className="field-input"
              value={y}
              onChange={(e) => setY(e.target.value)}
            />
          </div>
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
        <div className="field" style={{ display: 'flex', gap: 12 }}>
          <label style={{ display: 'flex', alignItems: 'center', gap: 4, fontSize: 12 }}>
            <input type="checkbox" checked={movable} onChange={(e) => setMovable(e.target.checked)} />
            可移動
          </label>
          <label style={{ display: 'flex', alignItems: 'center', gap: 4, fontSize: 12 }}>
            <input type="checkbox" checked={resizable} onChange={(e) => setResizable(e.target.checked)} />
            可調整大小
          </label>
          <label style={{ display: 'flex', alignItems: 'center', gap: 4, fontSize: 12 }}>
            <input type="checkbox" checked={removable} onChange={(e) => setRemovable(e.target.checked)} />
            可移除
          </label>
        </div>

        <div className="canvas-dialog__actions">
          <button type="button" className="btn-secondary" onClick={onCancel}>
            取消
          </button>
          <button
            type="button"
            className="btn"
            disabled={submitting || component === ''}
            onClick={() => void handleSubmit()}
          >
            加入
          </button>
        </div>
      </div>
    </div>
  );
}
