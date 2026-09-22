// 讓掛在 F07 s-canvas 上的「看板本體」item（見 canvas/itemComponentRegistry.tsx）能取得
// BoardCanvasPage 已經解析好的 boardId／canEdit，不用重新從路由或成員清單推導。
// canEdit 沿用 BoardCanvasPage 的定義：board-membership.role 為 Owner／Member 為 true，
// Viewer 為 false（spec-canvas-layout.md 角色定義：Viewer 對應 r-canvas-viewer，僅能平移縮放與離開）。
//
// requestedCardId／requestCardDetail／clearRequestedCardDetail：s-cards-by-assignee（T-19，另一個
// 獨立 canvas item）「點擊清單中的卡片開啟 F01 s-card-detail」需要跨 item 通知「看板本體」item 開啟
// 指定卡片的詳情（s-card-detail 本身是 BoardItemContent 內部狀態，不是獨立路由，見 OQ-T-19-fe-workload-04）；
// 兩個 item 在各自的 React 元件樹裡互不相通，只能靠共同祖先 BoardCanvasPage 提供的這個 context 橋接。
import { createContext, useContext } from 'react';

export interface BoardContextValue {
  boardId: string;
  canEdit: boolean;
  requestedCardId: string | null;
  requestCardDetail: (cardId: string) => void;
  clearRequestedCardDetail: () => void;
}

export const BoardContext = createContext<BoardContextValue | null>(null);

export function useBoardContext(): BoardContextValue {
  const value = useContext(BoardContext);
  if (value === null) {
    throw new Error('useBoardContext 必須在 BoardContext.Provider 內使用');
  }
  return value;
}
