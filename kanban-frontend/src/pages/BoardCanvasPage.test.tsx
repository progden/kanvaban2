// 對應 ui-canvas-layout.md s-canvas 操作表／驗收條件；透過 App 掛載（比照 BoardListPage.test.tsx），
// 因為畫面依賴 AuthProvider／ProtectedRoute，也需要 boardId 路由參數。
import { fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, describe, expect, it, vi } from 'vitest';
import App from '../App';
import { registerItemComponent, type ItemContentProps } from '../canvas/itemComponentRegistry';

function jsonResponse(body: unknown, status = 200) {
  return new Response(status === 204 ? null : JSON.stringify(body), { status });
}

function mockFetchByPath(responses: Record<string, () => Response>) {
  const calls: { url: string; body: unknown }[] = [];
  vi.spyOn(globalThis, 'fetch').mockImplementation((input, init) => {
    const url = typeof input === 'string' ? input : (input as Request).url;
    calls.push({ url, body: init?.body === undefined ? undefined : JSON.parse(init.body as string) });
    const path = Object.keys(responses).find((candidate) => url.endsWith(candidate));
    if (path === undefined) {
      throw new Error(`未預期的請求：${url}`);
    }
    return Promise.resolve(responses[path]());
  });
  return calls;
}

const SESSION_OK = () => jsonResponse({ username: 'user1', displayName: '陳柏翰' });

const BOARD_A = {
  id: 'board-a',
  name: '產品開發看板',
  createdBy: 'user1',
  swimlanes: [{ id: 'sw-1', name: '預設泳道', order: 0 }],
  stages: [{ id: 'st-1', name: '待辦', order: 0, role: 'NONE' }],
  clockTime: '2026-09-22T00:00:00Z',
  clockStatus: 'RUNNING',
};

const BOARD_ITEM = {
  id: 'item-1',
  component: 'board',
  anchor: 'canvas',
  x: 0,
  y: 0,
  width: 900,
  height: 600,
  z: 1,
  movable: true,
  resizable: true,
  removable: true,
};

function canvasResponse(items: unknown[] = [BOARD_ITEM], viewport: unknown = null) {
  return { id: 'canvas-1', boardId: 'board-a', zoomMin: 0.1, zoomMax: 4, items, viewport };
}

const MEMBERS_OWNER = [{ username: 'user1', displayName: '陳柏翰', role: 'OWNER' }];
const MEMBERS_VIEWER = [{ username: 'user1', displayName: '陳柏翰', role: 'VIEWER' }];

afterEach(() => {
  vi.restoreAllMocks();
});

async function renderCanvas(extra: Record<string, () => Response> = {}) {
  const calls = mockFetchByPath({
    '/api/session': SESSION_OK,
    '/api/boards/board-a': () => jsonResponse(BOARD_A),
    '/api/boards/board-a/canvas': () => jsonResponse(canvasResponse()),
    '/api/boards/board-a/members': () => jsonResponse(MEMBERS_OWNER),
    // T-14：item.component === 'board' 掛載真正的看板內容（BoardItemContent），
    // 載入時會額外打這兩支 API；本檔測的是 s-canvas 本身的行為，預設回傳空清單即可。
    '/api/boards/board-a/cards': () => jsonResponse([]),
    '/api/boards/board-a/assignee-candidates': () => jsonResponse([]),
    ...extra,
  });

  render(
    <MemoryRouter initialEntries={['/boards/board-a']}>
      <App />
    </MemoryRouter>,
  );

  await waitFor(() => expect(screen.getByTestId('canvas-item-item-1')).toBeInTheDocument());
  return calls;
}

describe('s-canvas', () => {
  it('開啟看板時觸發 uc-init-canvas，顯示畫布上全部元件', async () => {
    const calls = await renderCanvas();

    expect(calls.some((c) => c.url.endsWith('/api/boards/board-a/canvas'))).toBe(true);
    const item = screen.getByTestId('canvas-item-item-1');
    expect(within(item).getByText('board')).toBeInTheDocument();
    expect(item.style.left).toBe('0px');
    expect(item.style.top).toBe('0px');
    expect(item.style.width).toBe('900px');
    expect(item.style.height).toBe('600px');
  });

  it('移動元件後觸發 uc-move-item，元件顯示於新位置', async () => {
    const calls = await renderCanvas({
      '/api/canvas-items/item-1/move': () => jsonResponse({ ...BOARD_ITEM, x: 100, y: 100 }),
    });

    const item = screen.getByTestId('canvas-item-item-1');
    fireEvent.mouseDown(item, { clientX: 50, clientY: 50 });
    fireEvent.mouseMove(window, { clientX: 150, clientY: 150 });
    fireEvent.mouseUp(window, { clientX: 150, clientY: 150 });

    await waitFor(() => expect(item.style.left).toBe('100px'));
    expect(item.style.top).toBe('100px');
    const moveCall = calls.find((c) => c.url.endsWith('/api/canvas-items/item-1/move'));
    expect(moveCall?.body).toEqual({ x: 100, y: 100 });
  });

  it('移動失敗時位置還原並顯示訊息', async () => {
    await renderCanvas({
      '/api/canvas-items/item-1/move': () => jsonResponse({ message: '此元素不可移動' }, 409),
    });

    const item = screen.getByTestId('canvas-item-item-1');
    fireEvent.mouseDown(item, { clientX: 50, clientY: 50 });
    fireEvent.mouseMove(window, { clientX: 150, clientY: 150 });
    fireEvent.mouseUp(window, { clientX: 150, clientY: 150 });

    await waitFor(() => expect(screen.getByRole('alert')).toHaveTextContent('此元素不可移動'));
    expect(item.style.left).toBe('0px');
    expect(item.style.top).toBe('0px');
  });

  it('調整大小後觸發 uc-resize-item', async () => {
    const calls = await renderCanvas({
      '/api/canvas-items/item-1/resize': () => jsonResponse({ ...BOARD_ITEM, width: 1000, height: 700 }),
    });

    fireEvent.mouseDown(screen.getByTestId('canvas-item-item-1'), { clientX: 0, clientY: 0 });
    fireEvent.mouseUp(window, { clientX: 0, clientY: 0 });

    const handle = screen.getByTestId('resize-handle-item-1-se');
    fireEvent.mouseDown(handle, { clientX: 900, clientY: 600 });
    fireEvent.mouseMove(window, { clientX: 1000, clientY: 700 });
    fireEvent.mouseUp(window, { clientX: 1000, clientY: 700 });

    await waitFor(() => expect(screen.getByTestId('canvas-item-item-1').style.width).toBe('1000px'));
    const resizeCall = calls.find((c) => c.url.endsWith('/api/canvas-items/item-1/resize'));
    expect(resizeCall?.body).toEqual({ x: 0, y: 0, width: 1000, height: 700 });
  });

  it('移除元件前顯示確認，確認後觸發 uc-remove-item', async () => {
    const calls = await renderCanvas({
      '/api/canvas-items/item-1': () => jsonResponse(undefined, 204),
    });

    fireEvent.mouseDown(screen.getByTestId('canvas-item-item-1'), { clientX: 0, clientY: 0 });
    fireEvent.mouseUp(window, { clientX: 0, clientY: 0 });

    fireEvent.click(screen.getByRole('button', { name: '移除' }));
    const heading = screen.getByRole('heading', { name: '移除這個元件？' });
    expect(heading).toBeInTheDocument();

    const dialogPanel = heading.closest('.dialog-panel');
    if (dialogPanel === null) {
      throw new Error('找不到確認對話框');
    }
    fireEvent.click(within(dialogPanel as HTMLElement).getByRole('button', { name: '移除' }));

    await waitFor(() => expect(screen.queryByTestId('canvas-item-item-1')).not.toBeInTheDocument());
    expect(calls.some((c) => c.url.endsWith('/api/canvas-items/item-1') && c.body === undefined)).toBe(true);
  });

  it('置頂觸發 uc-reorder-item', async () => {
    const calls = await renderCanvas({
      '/api/canvas-items/item-1/reorder': () => jsonResponse({ ...BOARD_ITEM, z: 9 }),
    });

    fireEvent.mouseDown(screen.getByTestId('canvas-item-item-1'), { clientX: 0, clientY: 0 });
    fireEvent.mouseUp(window, { clientX: 0, clientY: 0 });
    fireEvent.click(screen.getByRole('button', { name: '置頂' }));

    await waitFor(() =>
      expect(calls.some((c) => c.url.endsWith('/api/canvas-items/item-1/reorder'))).toBe(true),
    );
    const reorderCall = calls.find((c) => c.url.endsWith('/api/canvas-items/item-1/reorder'));
    expect(reorderCall?.body).toEqual({ toFront: true });
  });

  it('放大縮小觸發 uc-set-viewport 並更新縮放百分比', async () => {
    const calls = await renderCanvas({
      '/api/boards/board-a/canvas/viewport': () => jsonResponse({ x: 0, y: 0, zoom: 1.25 }),
    });

    expect(screen.getByTestId('viewport-zoom')).toHaveTextContent('100%');
    fireEvent.click(screen.getByRole('button', { name: '放大' }));

    await waitFor(() => expect(screen.getByTestId('viewport-zoom')).toHaveTextContent('125%'));
    const viewportCall = calls.find((c) => c.url.endsWith('/api/boards/board-a/canvas/viewport'));
    expect(viewportCall?.body).toEqual({ x: 0, y: 0, zoom: 1.25 });
  });

  it('批次移動觸發 uc-move-items', async () => {
    const secondItem = { ...BOARD_ITEM, id: 'item-2', x: 500, y: 200 };
    const calls = await renderCanvas({
      '/api/boards/board-a/canvas': () => jsonResponse(canvasResponse([BOARD_ITEM, secondItem])),
      '/api/boards/board-a/canvas/items/move-batch': () =>
        jsonResponse([
          { ...BOARD_ITEM, x: 50, y: 30 },
          { ...secondItem, x: 550, y: 230 },
        ]),
    });

    fireEvent.mouseDown(screen.getByTestId('canvas-item-item-1'), { clientX: 0, clientY: 0 });
    fireEvent.mouseUp(window, { clientX: 0, clientY: 0 });
    fireEvent.mouseDown(screen.getByTestId('canvas-item-item-2'), { clientX: 500, clientY: 200, shiftKey: true });
    fireEvent.mouseMove(window, { clientX: 550, clientY: 230, shiftKey: true });
    fireEvent.mouseUp(window, { clientX: 550, clientY: 230, shiftKey: true });

    await waitFor(() => expect(screen.getByTestId('canvas-item-item-1').style.left).toBe('50px'));
    const batchCall = calls.find((c) => c.url.endsWith('/api/boards/board-a/canvas/items/move-batch'));
    expect(batchCall?.body).toEqual({ itemIds: ['item-1', 'item-2'], dx: 50, dy: 30 });
  });

  it('操作者角色為檢視者時，僅能平移縮放，選取與編輯操作皆不可用', async () => {
    await renderCanvas({ '/api/boards/board-a/members': () => jsonResponse(MEMBERS_VIEWER) });

    expect(screen.queryByRole('button', { name: '＋ 加入元件' })).not.toBeInTheDocument();

    fireEvent.mouseDown(screen.getByTestId('canvas-item-item-1'), { clientX: 0, clientY: 0 });
    expect(screen.queryByRole('button', { name: '移除' })).not.toBeInTheDocument();
  });

  it('放置元件成功後顯示於畫布，觸發 uc-place-item，帶上位置與能力欄位', async () => {
    const calls = await renderCanvas({
      '/api/boards/board-a/canvas/items': () =>
        jsonResponse(
          { id: 'item-2', component: '銷售圖表', anchor: 'canvas', x: 100, y: 200, width: 300, height: 200, z: 2, movable: false, resizable: false, removable: false },
          201,
        ),
    });

    fireEvent.click(screen.getByRole('button', { name: '＋ 加入元件' }));
    fireEvent.change(screen.getByLabelText('元件識別碼'), { target: { value: '銷售圖表' } });
    fireEvent.change(screen.getByLabelText('X'), { target: { value: '100' } });
    fireEvent.change(screen.getByLabelText('Y'), { target: { value: '200' } });
    fireEvent.click(screen.getByLabelText('可移動'));
    fireEvent.click(screen.getByLabelText('可調整大小'));
    fireEvent.click(screen.getByLabelText('可移除'));
    fireEvent.click(screen.getByRole('button', { name: '加入' }));

    await waitFor(() => expect(screen.getByTestId('canvas-item-item-2')).toBeInTheDocument());
    const placeCall = calls.find((c) => c.url.endsWith('/api/boards/board-a/canvas/items'));
    expect(placeCall?.body).toEqual({
      component: '銷售圖表',
      x: 100,
      y: 200,
      width: 300,
      height: 200,
      anchor: 'canvas',
      movable: false,
      resizable: false,
      removable: false,
    });
  });

  it('設定元件能力觸發 uc-set-item-capabilities，帶上三項能力欄位', async () => {
    const calls = await renderCanvas({
      '/api/canvas-items/item-1/capabilities': () =>
        jsonResponse({ ...BOARD_ITEM, movable: false, resizable: true, removable: true }),
    });

    fireEvent.mouseDown(screen.getByTestId('canvas-item-item-1'), { clientX: 0, clientY: 0 });
    fireEvent.mouseUp(window, { clientX: 0, clientY: 0 });
    fireEvent.click(screen.getByRole('button', { name: '可移動：開' }));

    await waitFor(() =>
      expect(calls.some((c) => c.url.endsWith('/api/canvas-items/item-1/capabilities'))).toBe(true),
    );
    const capabilitiesCall = calls.find((c) => c.url.endsWith('/api/canvas-items/item-1/capabilities'));
    expect(capabilitiesCall?.body).toEqual({ movable: false, resizable: true, removable: true });
  });

  it('掛載的元件內容收到的 itemId 為該 item 的 id，不是 item.component', async () => {
    const received: ItemContentProps[] = [];
    registerItemComponent('board', (props) => {
      received.push(props);
      return null;
    });

    await renderCanvas();

    expect(received.length).toBeGreaterThan(0);
    expect(received[0].itemId).toBe('item-1');
    expect(received[0].component).toBe('board');
  });

  it('z-index：z 為 0 或負值的元件仍渲染於背景之上，且不高於加入元件按鈕與浮動工具列', async () => {
    const zeroZItem = { ...BOARD_ITEM, z: 0 };
    const negativeZItem = { ...BOARD_ITEM, id: 'item-2', x: 500, z: -1 };
    await renderCanvas({
      '/api/boards/board-a/canvas': () => jsonResponse(canvasResponse([zeroZItem, negativeZItem])),
    });

    const item1 = screen.getByTestId('canvas-item-item-1');
    const item2 = screen.getByTestId('canvas-item-item-2');
    expect(Number(item1.style.zIndex)).toBeGreaterThan(0);
    expect(Number(item2.style.zIndex)).toBeGreaterThan(0);

    const addButton = screen.getByRole('button', { name: '＋ 加入元件' }).closest('.canvas-add-button') as HTMLElement;
    expect(Number(addButton.style.zIndex)).toBeGreaterThan(Number(item1.style.zIndex));
    expect(Number(addButton.style.zIndex)).toBeGreaterThan(Number(item2.style.zIndex));
  });

  it('浮動工具列：選取 y=0 的元件時，工具列 top 不為負值', async () => {
    await renderCanvas();

    fireEvent.mouseDown(screen.getByTestId('canvas-item-item-1'), { clientX: 0, clientY: 0 });
    fireEvent.mouseUp(window, { clientX: 0, clientY: 0 });

    const toolbar = screen.getByRole('button', { name: '置頂' }).closest('.canvas-toolbar') as HTMLElement;
    expect(Number(toolbar.style.top.replace('px', ''))).toBeGreaterThanOrEqual(0);
    expect(Number(toolbar.style.zIndex)).toBeGreaterThan(Number(screen.getByTestId('canvas-item-item-1').style.zIndex));
  });

  it('設定錨定方式為固定於畫面觸發 uc-set-item-anchor，帶上依檢視區換算後的位置與大小', async () => {
    const viewport = { x: 10, y: 20, zoom: 1.25 };
    const calls = await renderCanvas({
      '/api/boards/board-a/canvas': () => jsonResponse(canvasResponse([BOARD_ITEM], viewport)),
      '/api/canvas-items/item-1/anchor': () =>
        jsonResponse({ ...BOARD_ITEM, anchor: 'screen', x: -12.5, y: -25, width: 1125, height: 750 }),
    });

    fireEvent.mouseDown(screen.getByTestId('canvas-item-item-1'), { clientX: 0, clientY: 0 });
    fireEvent.mouseUp(window, { clientX: 0, clientY: 0 });
    fireEvent.click(screen.getByRole('button', { name: '固定於畫面' }));

    await waitFor(() =>
      expect(calls.some((c) => c.url.endsWith('/api/canvas-items/item-1/anchor'))).toBe(true),
    );
    const anchorCall = calls.find((c) => c.url.endsWith('/api/canvas-items/item-1/anchor'));
    expect(anchorCall?.body).toEqual({ anchor: 'screen', x: -12.5, y: -25, width: 1125, height: 750 });

    const item = screen.getByTestId('canvas-item-item-1');
    await waitFor(() => expect(item.style.left).toBe('-12.5px'));
    expect(screen.getByRole('button', { name: '錨定於畫布' })).toBeInTheDocument();
  });

  it('批次移除前顯示確認，確認後觸發 uc-remove-items，所選元件皆從畫面移除', async () => {
    const secondItem = { ...BOARD_ITEM, id: 'item-2', x: 500, y: 200 };
    const calls = await renderCanvas({
      '/api/boards/board-a/canvas': () => jsonResponse(canvasResponse([BOARD_ITEM, secondItem])),
      '/api/boards/board-a/canvas/items/remove-batch': () => jsonResponse(undefined, 204),
    });

    fireEvent.mouseDown(screen.getByTestId('canvas-item-item-1'), { clientX: 0, clientY: 0 });
    fireEvent.mouseUp(window, { clientX: 0, clientY: 0 });
    fireEvent.mouseDown(screen.getByTestId('canvas-item-item-2'), { clientX: 500, clientY: 200, shiftKey: true });
    fireEvent.mouseUp(window, { clientX: 500, clientY: 200, shiftKey: true });

    fireEvent.click(screen.getByRole('button', { name: '移除' }));
    const heading = screen.getByRole('heading', { name: '移除這 2 個元件？' });
    expect(heading).toBeInTheDocument();

    const dialogPanel = heading.closest('.dialog-panel');
    if (dialogPanel === null) {
      throw new Error('找不到確認對話框');
    }
    fireEvent.click(within(dialogPanel as HTMLElement).getByRole('button', { name: '移除' }));

    await waitFor(() => expect(screen.queryByTestId('canvas-item-item-1')).not.toBeInTheDocument());
    expect(screen.queryByTestId('canvas-item-item-2')).not.toBeInTheDocument();
    const batchCall = calls.find((c) => c.url.endsWith('/api/boards/board-a/canvas/items/remove-batch'));
    expect(batchCall?.body).toEqual({ itemIds: ['item-1', 'item-2'] });
  });

  it('批次移除失敗時顯示後端訊息，所選元件仍在畫面上', async () => {
    const secondItem = { ...BOARD_ITEM, id: 'item-2', x: 500, y: 200 };
    await renderCanvas({
      '/api/boards/board-a/canvas': () => jsonResponse(canvasResponse([BOARD_ITEM, secondItem])),
      '/api/boards/board-a/canvas/items/remove-batch': () => jsonResponse({ message: '這些元件不可移除' }, 409),
    });

    fireEvent.mouseDown(screen.getByTestId('canvas-item-item-1'), { clientX: 0, clientY: 0 });
    fireEvent.mouseUp(window, { clientX: 0, clientY: 0 });
    fireEvent.mouseDown(screen.getByTestId('canvas-item-item-2'), { clientX: 500, clientY: 200, shiftKey: true });
    fireEvent.mouseUp(window, { clientX: 500, clientY: 200, shiftKey: true });

    fireEvent.click(screen.getByRole('button', { name: '移除' }));
    const heading = screen.getByRole('heading', { name: '移除這 2 個元件？' });
    const dialogPanel = heading.closest('.dialog-panel');
    if (dialogPanel === null) {
      throw new Error('找不到確認對話框');
    }
    fireEvent.click(within(dialogPanel as HTMLElement).getByRole('button', { name: '移除' }));

    await waitFor(() =>
      expect(within(dialogPanel as HTMLElement).getByRole('alert')).toHaveTextContent('這些元件不可移除'),
    );
    expect(screen.getByTestId('canvas-item-item-1')).toBeInTheDocument();
    expect(screen.getByTestId('canvas-item-item-2')).toBeInTheDocument();
  });
});
