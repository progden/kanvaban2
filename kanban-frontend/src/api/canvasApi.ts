// 對應 kanban-spring CanvasController（spec-canvas-layout.md「看板畫布初始化」「元件放置」
// 「畫布元素排列」「畫布元素批次操作」「檢視區」）的請求／回應形狀，欄位名稱與後端 record 保持一致。
import { apiClient } from './http';

export type ItemAnchor = 'canvas' | 'screen';

export interface ItemView {
  id: string;
  component: string;
  anchor: ItemAnchor;
  x: number;
  y: number;
  width: number;
  height: number;
  z: number;
  movable: boolean;
  resizable: boolean;
  removable: boolean;
}

export interface ViewportView {
  x: number;
  y: number;
  zoom: number;
}

export interface CanvasView {
  id: string;
  boardId: string;
  zoomMin: number;
  zoomMax: number;
  items: ItemView[];
  viewport: ViewportView | null;
}

export interface PlaceItemRequest {
  component: string;
  x: number;
  y: number;
  width: number;
  height: number;
  anchor?: ItemAnchor;
  movable?: boolean;
  resizable?: boolean;
  removable?: boolean;
}

export function openCanvas(boardId: string): Promise<CanvasView> {
  return apiClient.get<CanvasView>(`/api/boards/${boardId}/canvas`);
}

export function placeItem(boardId: string, request: PlaceItemRequest): Promise<ItemView> {
  return apiClient.post<ItemView>(`/api/boards/${boardId}/canvas/items`, request);
}

export function removeItem(itemId: string): Promise<void> {
  return apiClient.del<void>(`/api/canvas-items/${itemId}`);
}

export function moveItem(itemId: string, x: number, y: number): Promise<ItemView> {
  return apiClient.post<ItemView>(`/api/canvas-items/${itemId}/move`, { x, y });
}

export function resizeItem(itemId: string, x: number, y: number, width: number, height: number): Promise<ItemView> {
  return apiClient.post<ItemView>(`/api/canvas-items/${itemId}/resize`, { x, y, width, height });
}

export function setItemCapabilities(
  itemId: string,
  movable: boolean,
  resizable: boolean,
  removable: boolean,
): Promise<ItemView> {
  return apiClient.patch<ItemView>(`/api/canvas-items/${itemId}/capabilities`, { movable, resizable, removable });
}

export function setItemAnchor(
  itemId: string,
  anchor: ItemAnchor,
  x: number,
  y: number,
  width: number,
  height: number,
): Promise<ItemView> {
  return apiClient.patch<ItemView>(`/api/canvas-items/${itemId}/anchor`, { anchor, x, y, width, height });
}

export function reorderItem(itemId: string, toFront: boolean): Promise<ItemView> {
  return apiClient.post<ItemView>(`/api/canvas-items/${itemId}/reorder`, { toFront });
}

export function moveItems(boardId: string, itemIds: string[], dx: number, dy: number): Promise<ItemView[]> {
  return apiClient.post<ItemView[]>(`/api/boards/${boardId}/canvas/items/move-batch`, { itemIds, dx, dy });
}

export function removeItems(boardId: string, itemIds: string[]): Promise<void> {
  return apiClient.post<void>(`/api/boards/${boardId}/canvas/items/remove-batch`, { itemIds });
}

export function setViewport(boardId: string, x: number, y: number, zoom: number): Promise<ViewportView> {
  return apiClient.put<ViewportView>(`/api/boards/${boardId}/canvas/viewport`, { x, y, zoom });
}
