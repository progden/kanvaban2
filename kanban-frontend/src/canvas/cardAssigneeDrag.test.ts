// 驗證 s-workload-dashboard「拖曳成員頭像到卡片追加負責人」的拖放協定本體。
// 拖放目標（T-14-fe-board-item 的卡片縮圖）尚未存在，這裡用最小的假 DataTransfer 模擬拖放目標端
// 接到 drop 事件後呼叫 handleCardAssigneeDrop 的行為，驗證來源／目標兩端的協定可以正確串起來。
import { afterEach, describe, expect, it, vi } from 'vitest';
import {
  acceptsCardAssigneeDrop,
  CARD_ASSIGNEE_DRAG_MIME,
  handleCardAssigneeDrop,
  startUserAvatarDrag,
  type DragDataCarrier,
} from './cardAssigneeDrag';

function fakeDragEvent(): DragDataCarrier & { store: Map<string, string> } {
  const store = new Map<string, string>();
  return {
    store,
    dataTransfer: {
      getData: (format: string) => store.get(format) ?? '',
      setData: (format: string, data: string) => {
        store.set(format, data);
      },
      get types() {
        return Array.from(store.keys());
      },
      effectAllowed: undefined,
    },
  };
}

afterEach(() => {
  vi.restoreAllMocks();
});

describe('cardAssigneeDrag', () => {
  it('拖曳開始時把使用者 ID 放進 dataTransfer', () => {
    const e = fakeDragEvent();
    startUserAvatarDrag(e, 'user-1');
    expect(e.dataTransfer.getData(CARD_ASSIGNEE_DRAG_MIME)).toBe('user-1');
    expect(e.dataTransfer.getData('text/plain')).toBe('user-1');
  });

  it('拖放目標可以用 acceptsCardAssigneeDrop 判斷是否為本協定的拖曳', () => {
    const e = fakeDragEvent();
    startUserAvatarDrag(e, 'user-1');
    expect(acceptsCardAssigneeDrop(e)).toBe(true);
    expect(acceptsCardAssigneeDrop(fakeDragEvent())).toBe(false);
  });

  it('drop 時呼叫拖曳頭像追加卡片負責人端點', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(JSON.stringify({ id: 'card-b', title: 'B' }), { status: 200 }),
    );
    const e = fakeDragEvent();
    startUserAvatarDrag(e, 'user-1');

    await handleCardAssigneeDrop(e, 'card-b');

    expect(fetchSpy).toHaveBeenCalledWith(
      '/api/cards/card-b/assignees/drag',
      expect.objectContaining({ method: 'POST', body: JSON.stringify({ userId: 'user-1' }) }),
    );
  });

  it('沒有攜帶使用者 ID 時不呼叫 API', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch');
    await handleCardAssigneeDrop(fakeDragEvent(), 'card-b');
    expect(fetchSpy).not.toHaveBeenCalled();
  });
});
