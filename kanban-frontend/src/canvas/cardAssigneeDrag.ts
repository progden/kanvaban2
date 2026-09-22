// spec-workload.md「拖曳成員頭像到卡片上，追加該成員為負責人」（uc-drag-assign-card-owner）的
// 拖放協定。依 ui-authoring-loop OQ-50 定案：拖放目標是同一畫布上 F01 s-board item 的卡片縮圖，
// 但 s-board item 內容由 T-14-fe-board-item 實作（本任務動工時尚未合併，見交接摘要／OQ）。
// 拖曳來源（本模組 WorkloadDashboard）與拖放目標分屬不同 canvas item 的獨立 React 元件樹，
// 彼此不共用 state，只能靠瀏覽器原生 HTML5 drag-and-drop 事件溝通；這裡把來源／目標兩端的協定
// 抽成共用函式，T-14 的卡片縮圖節點只需在 onDrop 呼叫 handleCardAssigneeDrop。
import { dragAssignCardOwner } from '../api/cardApi';

export const CARD_ASSIGNEE_DRAG_MIME = 'application/x-kanban-user-id';

// 用最小需要的形狀描述 DataTransfer，脫離具體的 DOM Event 型別，方便測試與未來替換傳輸方式。
export interface DragDataCarrier {
  dataTransfer: {
    getData(format: string): string;
    setData(format: string, data: string): void;
    types?: readonly string[];
    effectAllowed?: string;
  };
}

export function startUserAvatarDrag(e: DragDataCarrier, userId: string): void {
  e.dataTransfer.setData(CARD_ASSIGNEE_DRAG_MIME, userId);
  e.dataTransfer.setData('text/plain', userId);
  e.dataTransfer.effectAllowed = 'copy';
}

export function acceptsCardAssigneeDrop(e: DragDataCarrier): boolean {
  const types = e.dataTransfer.types;
  return types === undefined || types.includes(CARD_ASSIGNEE_DRAG_MIME);
}

export async function handleCardAssigneeDrop(e: DragDataCarrier, cardId: string): Promise<void> {
  const userId = e.dataTransfer.getData(CARD_ASSIGNEE_DRAG_MIME) || e.dataTransfer.getData('text/plain');
  if (userId === '') {
    return;
  }
  await dragAssignCardOwner(cardId, userId);
}
