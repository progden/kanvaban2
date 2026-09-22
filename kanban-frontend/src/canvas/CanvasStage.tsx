// s-canvas 本體：spec-canvas-layout.md「元件放置」「畫布元素排列」「畫布元素批次操作」「檢視區」
// 對應的畫面。版面依 .dev/ui-prototype/Main.dc.html／CanvasPanel.dc.html（格線背景、卡片式元素、
// 選取後浮動工具列、八個調整大小把手、右下角縮放控制），行為依 ui-canvas-layout.md 操作表／驗收條件。
import { useCallback, useEffect, useRef, useState } from 'react';
import type { ItemAnchor, ItemView, ViewportView } from '../api/canvasApi';
import * as canvasApi from '../api/canvasApi';
import { ApiError } from '../api/http';
import { clampZoom, convertBoxForAnchor, itemToScreenBox, pixelDeltaToAnchorDelta } from './geometry';
import { resolveItemComponent } from './itemComponentRegistry';
import { PlaceItemDialog } from './PlaceItemDialog';
import { RemoveItemsDialog } from './RemoveItemsDialog';
import './CanvasStage.css';

const HANDLES = ['n', 's', 'e', 'w', 'ne', 'nw', 'se', 'sw'] as const;
type Handle = (typeof HANDLES)[number];

const ZOOM_STEP = 1.25;
const CLICK_THRESHOLD_PX = 4;
// z-index 分層：畫布元素依 item.z 排序後取相對名次（避免 0／負值被格線背景蓋住），
// 畫面固定元素整組高於畫布元素，畫面固定的操作介面（加入元件、浮動工具列、縮放控制、錯誤訊息）
// 再高於全部 item，確保 ui-canvas-layout.md 操作表的入口不會被任何元素蓋住或裁切。
const CANVAS_ITEM_Z_BASE = 1;
const SCREEN_ITEM_Z_BASE = 100000;
const CHROME_Z = 200000;
const TOOLBAR_MIN_TOP = 4;

interface MoveDrag {
  kind: 'move';
  startX: number;
  startY: number;
  ids: string[];
  anchor: ItemAnchor;
  originals: Map<string, { x: number; y: number }>;
  moved: boolean;
}

interface ResizeDrag {
  kind: 'resize';
  startX: number;
  startY: number;
  id: string;
  anchor: ItemAnchor;
  handle: Handle;
  original: { x: number; y: number; width: number; height: number };
  moved: boolean;
}

interface PanDrag {
  kind: 'pan';
  startX: number;
  startY: number;
  original: ViewportView;
  moved: boolean;
}

type DragSession = MoveDrag | ResizeDrag | PanDrag;

interface CanvasStageProps {
  boardId: string;
  zoomMin: number;
  zoomMax: number;
  initialItems: ItemView[];
  initialViewport: ViewportView | null;
  canEdit: boolean;
}

export function CanvasStage({
  boardId,
  zoomMin,
  zoomMax,
  initialItems,
  initialViewport,
  canEdit,
}: CanvasStageProps) {
  const [items, setItems] = useState<ItemView[]>(initialItems);
  const [viewport, setViewportState] = useState<ViewportView>(initialViewport ?? { x: 0, y: 0, zoom: 1 });
  const [selectedIds, setSelectedIds] = useState<string[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [placeDialogOpen, setPlaceDialogOpen] = useState(false);
  const [removeTarget, setRemoveTarget] = useState<string[] | null>(null);
  const [isPanning, setIsPanning] = useState(false);

  const stageRef = useRef<HTMLDivElement | null>(null);
  const dragRef = useRef<DragSession | null>(null);
  const itemsRef = useRef(items);
  const viewportRef = useRef(viewport);

  useEffect(() => {
    itemsRef.current = items;
  }, [items]);
  useEffect(() => {
    viewportRef.current = viewport;
  }, [viewport]);

  const itemById = useCallback((id: string) => itemsRef.current.find((i) => i.id === id), []);

  const handleWindowMouseMove = useCallback((e: MouseEvent) => {
    const drag = dragRef.current;
    if (drag === null) {
      return;
    }
    const dxPx = e.clientX - drag.startX;
    const dyPx = e.clientY - drag.startY;
    if (Math.abs(dxPx) > CLICK_THRESHOLD_PX || Math.abs(dyPx) > CLICK_THRESHOLD_PX) {
      drag.moved = true;
    }

    if (drag.kind === 'move') {
      const { dx, dy } = pixelDeltaToAnchorDelta(dxPx, dyPx, drag.anchor, viewportRef.current.zoom);
      setItems((current) =>
        current.map((item) => {
          const original = drag.originals.get(item.id);
          if (original === undefined) {
            return item;
          }
          return { ...item, x: original.x + dx, y: original.y + dy };
        }),
      );
    } else if (drag.kind === 'resize') {
      const { dx, dy } = pixelDeltaToAnchorDelta(dxPx, dyPx, drag.anchor, viewportRef.current.zoom);
      const { x: ox, y: oy, width: ow, height: oh } = drag.original;
      let x = ox;
      let y = oy;
      let width = ow;
      let height = oh;
      if (drag.handle.includes('n')) {
        y = oy + dy;
        height = Math.max(1, oh - dy);
      }
      if (drag.handle.includes('s')) {
        height = Math.max(1, oh + dy);
      }
      if (drag.handle.includes('w')) {
        x = ox + dx;
        width = Math.max(1, ow - dx);
      }
      if (drag.handle.includes('e')) {
        width = Math.max(1, ow + dx);
      }
      setItems((current) => current.map((item) => (item.id === drag.id ? { ...item, x, y, width, height } : item)));
    } else {
      const newViewport = {
        x: drag.original.x - dxPx / drag.original.zoom,
        y: drag.original.y - dyPx / drag.original.zoom,
        zoom: drag.original.zoom,
      };
      setViewportState(newViewport);
    }
  }, []);

  const commitMoveDrag = useCallback(
    async (drag: MoveDrag) => {
      const current = itemsRef.current.filter((i) => drag.ids.includes(i.id));
      try {
        if (drag.ids.length === 1) {
          const item = current[0];
          const updated = await canvasApi.moveItem(item.id, item.x, item.y);
          setItems((cur) => cur.map((i) => (i.id === updated.id ? updated : i)));
        } else {
          const first = current[0];
          const original = drag.originals.get(first.id);
          if (original === undefined) {
            return;
          }
          const dx = first.x - original.x;
          const dy = first.y - original.y;
          const updated = await canvasApi.moveItems(boardId, drag.ids, dx, dy);
          const byId = new Map(updated.map((i) => [i.id, i]));
          setItems((cur) => cur.map((i) => byId.get(i.id) ?? i));
        }
      } catch (e) {
        setItems((cur) =>
          cur.map((i) => {
            const original = drag.originals.get(i.id);
            return original === undefined ? i : { ...i, x: original.x, y: original.y };
          }),
        );
        setError(e instanceof ApiError ? e.message : '移動元件失敗，請稍後再試');
      }
    },
    [boardId],
  );

  const commitResizeDrag = useCallback(async (drag: ResizeDrag) => {
    const item = itemById(drag.id);
    if (item === undefined) {
      return;
    }
    try {
      const updated = await canvasApi.resizeItem(item.id, item.x, item.y, item.width, item.height);
      setItems((cur) => cur.map((i) => (i.id === updated.id ? updated : i)));
    } catch (e) {
      setItems((cur) => cur.map((i) => (i.id === drag.id ? { ...i, ...drag.original } : i)));
      setError(e instanceof ApiError ? e.message : '調整大小失敗，請稍後再試');
    }
  }, [itemById]);

  const commitPanDrag = useCallback(
    async (drag: PanDrag) => {
      try {
        const updated = await canvasApi.setViewport(boardId, viewportRef.current.x, viewportRef.current.y, viewportRef.current.zoom);
        setViewportState(updated);
      } catch (e) {
        setViewportState(drag.original);
        setError(e instanceof ApiError ? e.message : '設定檢視區失敗，請稍後再試');
      }
    },
    [boardId],
  );

  const handleWindowMouseUp = useCallback(() => {
    const drag = dragRef.current;
    if (drag === null) {
      return;
    }
    dragRef.current = null;
    setIsPanning(false);
    window.removeEventListener('mousemove', handleWindowMouseMove);
    window.removeEventListener('mouseup', handleWindowMouseUp);
    if (!drag.moved) {
      if (drag.kind === 'pan') {
        setSelectedIds([]);
      }
      return;
    }
    if (drag.kind === 'move') {
      void commitMoveDrag(drag);
    } else if (drag.kind === 'resize') {
      void commitResizeDrag(drag);
    } else {
      void commitPanDrag(drag);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [commitMoveDrag, commitResizeDrag, commitPanDrag, handleWindowMouseMove]);

  useEffect(
    () => () => {
      window.removeEventListener('mousemove', handleWindowMouseMove);
      window.removeEventListener('mouseup', handleWindowMouseUp);
    },
    [handleWindowMouseMove, handleWindowMouseUp],
  );

  function startDrag(e: React.MouseEvent, drag: DragSession) {
    dragRef.current = drag;
    window.addEventListener('mousemove', handleWindowMouseMove);
    window.addEventListener('mouseup', handleWindowMouseUp);
    e.preventDefault();
  }

  function handleItemMouseDown(e: React.MouseEvent, item: ItemView) {
    if (!canEdit) {
      return;
    }
    e.stopPropagation();
    const isSelected = selectedIds.includes(item.id);
    let nextSelected: string[];
    if (e.shiftKey) {
      nextSelected = isSelected ? selectedIds.filter((id) => id !== item.id) : [...selectedIds, item.id];
    } else if (isSelected) {
      nextSelected = selectedIds;
    } else {
      nextSelected = [item.id];
    }
    setSelectedIds(nextSelected);

    if (!item.movable || !nextSelected.includes(item.id)) {
      return;
    }
    const originals = new Map(nextSelected.map((id) => {
      const found = itemById(id);
      return [id, { x: found?.x ?? 0, y: found?.y ?? 0 }] as const;
    }));
    startDrag(e, {
      kind: 'move',
      startX: e.clientX,
      startY: e.clientY,
      ids: nextSelected,
      anchor: item.anchor,
      originals,
      moved: false,
    });
  }

  function handleResizeHandleMouseDown(e: React.MouseEvent, item: ItemView, handle: Handle) {
    if (!canEdit || !item.resizable) {
      return;
    }
    e.stopPropagation();
    startDrag(e, {
      kind: 'resize',
      startX: e.clientX,
      startY: e.clientY,
      id: item.id,
      anchor: item.anchor,
      handle,
      original: { x: item.x, y: item.y, width: item.width, height: item.height },
      moved: false,
    });
  }

  function handleStageMouseDown(e: React.MouseEvent) {
    if (e.target !== e.currentTarget) {
      return;
    }
    setIsPanning(true);
    startDrag(e, { kind: 'pan', startX: e.clientX, startY: e.clientY, original: viewport, moved: false });
  }

  async function handleReorder(id: string, toFront: boolean) {
    try {
      const updated = await canvasApi.reorderItem(id, toFront);
      setItems((cur) => cur.map((i) => (i.id === updated.id ? updated : i)));
    } catch (e) {
      setError(e instanceof ApiError ? e.message : '調整層序失敗，請稍後再試');
    }
  }

  async function handleSetCapability(item: ItemView, patch: Partial<Pick<ItemView, 'movable' | 'resizable' | 'removable'>>) {
    const movable = patch.movable ?? item.movable;
    const resizable = patch.resizable ?? item.resizable;
    const removable = patch.removable ?? item.removable;
    try {
      const updated = await canvasApi.setItemCapabilities(item.id, movable, resizable, removable);
      setItems((cur) => cur.map((i) => (i.id === updated.id ? updated : i)));
    } catch (e) {
      setError(e instanceof ApiError ? e.message : '設定元件能力失敗，請稍後再試');
    }
  }

  async function handleToggleAnchor(item: ItemView) {
    const nextAnchor: ItemAnchor = item.anchor === 'canvas' ? 'screen' : 'canvas';
    const box = itemToScreenBox(item, viewport);
    const converted = convertBoxForAnchor(box, item.anchor, nextAnchor, viewport);
    try {
      const updated = await canvasApi.setItemAnchor(
        item.id,
        nextAnchor,
        converted.x,
        converted.y,
        converted.width,
        converted.height,
      );
      setItems((cur) => cur.map((i) => (i.id === updated.id ? updated : i)));
    } catch (e) {
      setError(e instanceof ApiError ? e.message : '設定錨定方式失敗，請稍後再試');
    }
  }

  async function handleConfirmRemove() {
    if (removeTarget === null) {
      return;
    }
    if (removeTarget.length === 1) {
      await canvasApi.removeItem(removeTarget[0]);
    } else {
      await canvasApi.removeItems(boardId, removeTarget);
    }
    const removedIds = new Set(removeTarget);
    setItems((cur) => cur.filter((i) => !removedIds.has(i.id)));
    setSelectedIds((cur) => cur.filter((id) => !removedIds.has(id)));
    setRemoveTarget(null);
  }

  async function handlePlaceItem(input: {
    component: string;
    x: number;
    y: number;
    width: number;
    height: number;
    anchor: ItemAnchor;
    movable: boolean;
    resizable: boolean;
    removable: boolean;
  }) {
    const created = await canvasApi.placeItem(boardId, input);
    setItems((cur) => [...cur, created]);
    setPlaceDialogOpen(false);
  }

  function applyZoom(factor: number) {
    const rect = stageRef.current?.getBoundingClientRect();
    const centerPx = { x: (rect?.width ?? 0) / 2, y: (rect?.height ?? 0) / 2 };
    const newZoom = clampZoom(viewport.zoom * factor, zoomMin, zoomMax);
    const cx = viewport.x + centerPx.x / viewport.zoom;
    const cy = viewport.y + centerPx.y / viewport.zoom;
    const newViewport = { x: cx - centerPx.x / newZoom, y: cy - centerPx.y / newZoom, zoom: newZoom };
    const previous = viewport;
    setViewportState(newViewport);
    canvasApi
      .setViewport(boardId, newViewport.x, newViewport.y, newViewport.zoom)
      .then((updated) => setViewportState(updated))
      .catch((e: unknown) => {
        setViewportState(previous);
        setError(e instanceof ApiError ? e.message : '設定檢視區失敗，請稍後再試');
      });
  }

  const selectedItems = items.filter((i) => selectedIds.includes(i.id));
  const singleSelected = selectedItems.length === 1 ? selectedItems[0] : null;

  const canvasRanks = new Map(
    items
      .filter((i) => i.anchor === 'canvas')
      .sort((a, b) => a.z - b.z)
      .map((i, idx) => [i.id, CANVAS_ITEM_Z_BASE + idx] as const),
  );
  const screenRanks = new Map(
    items
      .filter((i) => i.anchor === 'screen')
      .sort((a, b) => a.z - b.z)
      .map((i, idx) => [i.id, SCREEN_ITEM_Z_BASE + idx] as const),
  );

  return (
    <div
      data-testid="canvas-stage"
      className={`canvas-stage${isPanning ? ' canvas-stage--panning' : ''}`}
      ref={stageRef}
      onMouseDown={handleStageMouseDown}
    >
      {canEdit && (
        <div className="canvas-add-button" style={{ zIndex: CHROME_Z }} onMouseDown={(e) => e.stopPropagation()}>
          <button type="button" className="btn-sm" onClick={() => setPlaceDialogOpen(true)}>
            ＋ 加入元件
          </button>
        </div>
      )}

      {error !== null && (
        <p
          role="alert"
          className="form-error canvas-page__error"
          style={{ position: 'absolute', left: 68, top: 14, zIndex: CHROME_Z }}
        >
          {error}
        </p>
      )}

      {items.map((item) => {
        const box = itemToScreenBox(item, viewport);
        const selected = selectedIds.includes(item.id);
        const Content = resolveItemComponent(item.component);
        const zIndex = canvasRanks.get(item.id) ?? screenRanks.get(item.id) ?? CANVAS_ITEM_Z_BASE;
        return (
          <div
            key={item.id}
            data-testid={`canvas-item-${item.id}`}
            className={`canvas-item${selected ? ' canvas-item--selected' : ''}${item.movable ? ' canvas-item--movable' : ''}`}
            style={{ left: box.left, top: box.top, width: box.width, height: box.height, zIndex }}
            onMouseDown={(e) => handleItemMouseDown(e, item)}
          >
            <div className="canvas-item__header">
              <span>{item.component}</span>
            </div>
            <div className="canvas-item__body">
              <Content itemId={item.id} component={item.component} width={box.width} height={box.height} />
            </div>
            {selected && canEdit && selectedIds.length === 1 && item.resizable && (
              <>
                {(item.movable ? HANDLES : (['se'] as const)).map((handle) => (
                  <div
                    key={handle}
                    data-testid={`resize-handle-${item.id}-${handle}`}
                    className={`canvas-item__handle canvas-item__handle--${handle}`}
                    onMouseDown={(e) => handleResizeHandleMouseDown(e, item, handle)}
                  />
                ))}
              </>
            )}
          </div>
        );
      })}

      {canEdit && singleSelected !== null && (
        <div
          className="canvas-toolbar"
          style={{
            left: itemToScreenBox(singleSelected, viewport).left,
            top: Math.max(TOOLBAR_MIN_TOP, itemToScreenBox(singleSelected, viewport).top - 42),
            zIndex: CHROME_Z,
          }}
          onMouseDown={(e) => e.stopPropagation()}
        >
          <button type="button" className="btn-sm" onClick={() => void handleReorder(singleSelected.id, true)}>
            置頂
          </button>
          <button type="button" className="btn-sm" onClick={() => void handleReorder(singleSelected.id, false)}>
            置底
          </button>
          <div className="canvas-toolbar__divider" />
          <button
            type="button"
            className="btn-sm"
            aria-pressed={singleSelected.movable}
            onClick={() => void handleSetCapability(singleSelected, { movable: !singleSelected.movable })}
          >
            可移動：{singleSelected.movable ? '開' : '關'}
          </button>
          <button
            type="button"
            className="btn-sm"
            aria-pressed={singleSelected.resizable}
            onClick={() => void handleSetCapability(singleSelected, { resizable: !singleSelected.resizable })}
          >
            可調整大小：{singleSelected.resizable ? '開' : '關'}
          </button>
          <button
            type="button"
            className="btn-sm"
            aria-pressed={singleSelected.removable}
            onClick={() => void handleSetCapability(singleSelected, { removable: !singleSelected.removable })}
          >
            可移除：{singleSelected.removable ? '開' : '關'}
          </button>
          <div className="canvas-toolbar__divider" />
          <button type="button" className="btn-sm" onClick={() => void handleToggleAnchor(singleSelected)}>
            {singleSelected.anchor === 'canvas' ? '固定於畫面' : '錨定於畫布'}
          </button>
          <div className="canvas-toolbar__divider" />
          <button
            type="button"
            className="btn-sm canvas-toolbar__remove"
            disabled={!singleSelected.removable}
            onClick={() => setRemoveTarget([singleSelected.id])}
          >
            移除
          </button>
        </div>
      )}

      {canEdit && selectedItems.length > 1 && (
        <div
          className="canvas-toolbar"
          style={{ left: 16, top: 60, zIndex: CHROME_Z }}
          onMouseDown={(e) => e.stopPropagation()}
        >
          <span style={{ fontSize: 12 }}>已選取 {selectedItems.length} 個元件</span>
          <div className="canvas-toolbar__divider" />
          <button
            type="button"
            className="btn-sm canvas-toolbar__remove"
            onClick={() => setRemoveTarget(selectedItems.map((i) => i.id))}
          >
            移除
          </button>
        </div>
      )}

      <div className="canvas-viewport-controls" style={{ zIndex: CHROME_Z }} onMouseDown={(e) => e.stopPropagation()}>
        <button type="button" aria-label="縮小" onClick={() => applyZoom(1 / ZOOM_STEP)}>
          −
        </button>
        <span className="canvas-viewport-controls__zoom" data-testid="viewport-zoom">
          {Math.round(viewport.zoom * 100)}%
        </span>
        <button type="button" aria-label="放大" onClick={() => applyZoom(ZOOM_STEP)}>
          ＋
        </button>
      </div>

      {placeDialogOpen && (
        <PlaceItemDialog onCancel={() => setPlaceDialogOpen(false)} onSubmit={handlePlaceItem} />
      )}
      {removeTarget !== null && (
        <RemoveItemsDialog
          count={removeTarget.length}
          onCancel={() => setRemoveTarget(null)}
          onConfirm={handleConfirmRemove}
        />
      )}
    </div>
  );
}
