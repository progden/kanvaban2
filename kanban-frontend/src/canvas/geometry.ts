// s-canvas 的座標換算：畫布座標系（item.anchor === 'canvas'）與畫面座標系（'screen'）。
// 依 spec-canvas-layout.md「其他名詞」：畫面座標系以檢視區左上角為原點，平移／縮放畫布不影響它。
import type { ItemAnchor, ItemView, ViewportView } from '../api/canvasApi';

export interface ScreenBox {
  left: number;
  top: number;
  width: number;
  height: number;
}

export function itemToScreenBox(item: Pick<ItemView, 'anchor' | 'x' | 'y' | 'width' | 'height'>, viewport: ViewportView): ScreenBox {
  if (item.anchor === 'screen') {
    return { left: item.x, top: item.y, width: item.width, height: item.height };
  }
  return {
    left: (item.x - viewport.x) * viewport.zoom,
    top: (item.y - viewport.y) * viewport.zoom,
    width: item.width * viewport.zoom,
    height: item.height * viewport.zoom,
  };
}

// 把畫面像素位移換算為指定錨定方式下的座標系位移。
export function pixelDeltaToAnchorDelta(dxPx: number, dyPx: number, anchor: ItemAnchor, zoom: number) {
  if (anchor === 'screen') {
    return { dx: dxPx, dy: dyPx };
  }
  return { dx: dxPx / zoom, dy: dyPx / zoom };
}

// 錨定方式切換時，把目前顯示在畫面上的位置與大小換算成新錨定方式的座標系（spec：
// 「不由系統換算」，由呼叫端決定送出的值；這裡選擇讓元素切換後視覺位置不變）。
export function convertBoxForAnchor(
  box: ScreenBox,
  fromAnchor: ItemAnchor,
  toAnchor: ItemAnchor,
  viewport: ViewportView,
): { x: number; y: number; width: number; height: number } {
  if (fromAnchor === toAnchor) {
    return { x: box.left, y: box.top, width: box.width, height: box.height };
  }
  if (toAnchor === 'screen') {
    return { x: box.left, y: box.top, width: box.width, height: box.height };
  }
  return {
    x: viewport.x + box.left / viewport.zoom,
    y: viewport.y + box.top / viewport.zoom,
    width: box.width / viewport.zoom,
    height: box.height / viewport.zoom,
  };
}

export function clampZoom(zoom: number, zoomMin: number, zoomMax: number): number {
  return Math.min(zoomMax, Math.max(zoomMin, zoom));
}
