// 讓掛在 F07 s-canvas 上的「看板本體」item（見 canvas/itemComponentRegistry.tsx）能取得
// BoardCanvasPage 已經解析好的 boardId／canEdit，不用重新從路由或成員清單推導。
// canEdit 沿用 BoardCanvasPage 的定義：board-membership.role 為 Owner／Member 為 true，
// Viewer 為 false（spec-canvas-layout.md 角色定義：Viewer 對應 r-canvas-viewer，僅能平移縮放與離開）。
import { createContext, useContext } from 'react';

export interface BoardContextValue {
  boardId: string;
  canEdit: boolean;
}

export const BoardContext = createContext<BoardContextValue | null>(null);

export function useBoardContext(): BoardContextValue {
  const value = useContext(BoardContext);
  if (value === null) {
    throw new Error('useBoardContext 必須在 BoardContext.Provider 內使用');
  }
  return value;
}
