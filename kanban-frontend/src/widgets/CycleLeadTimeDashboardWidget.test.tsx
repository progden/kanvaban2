// 對應 ui-kanban-widgets.md s-cycle-lead-time-dashboard 操作表／驗收條件；比照
// pages/BoardCanvasPage.test.tsx 的作法透過 App + MemoryRouter 掛載一個 component 值為
// 's-cycle-lead-time-dashboard' 的 canvas item，驗證開啟畫面即觸發 uc-view-cycle-lead-time。
import { render, screen, waitFor, within } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, describe, expect, it, vi } from 'vitest';
import App from '../App';

function jsonResponse(body: unknown, status = 200) {
  return new Response(status === 204 ? null : JSON.stringify(body), { status });
}

function mockFetchByPath(responses: Record<string, () => Response>) {
  const calls: { url: string }[] = [];
  vi.spyOn(globalThis, 'fetch').mockImplementation((input) => {
    const url = typeof input === 'string' ? input : (input as Request).url;
    calls.push({ url });
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

const WIDGET_ITEM = {
  id: 'item-1',
  component: 's-cycle-lead-time-dashboard',
  anchor: 'canvas',
  x: 0,
  y: 0,
  width: 400,
  height: 300,
  z: 1,
  movable: true,
  resizable: true,
  removable: true,
};

const CANVAS_RESPONSE = { id: 'canvas-1', boardId: 'board-a', zoomMin: 0.1, zoomMax: 4, items: [WIDGET_ITEM], viewport: null };
const MEMBERS_OWNER = [{ username: 'user1', displayName: '陳柏翰', role: 'OWNER' }];

afterEach(() => {
  vi.restoreAllMocks();
});

describe('s-cycle-lead-time-dashboard', () => {
  it('開啟畫面時觸發 uc-view-cycle-lead-time，顯示卡片清單與統計摘要', async () => {
    const calls = mockFetchByPath({
      '/api/session': SESSION_OK,
      '/api/boards/board-a': () => jsonResponse(BOARD_A),
      '/api/boards/board-a/canvas': () => jsonResponse(CANVAS_RESPONSE),
      '/api/boards/board-a/members': () => jsonResponse(MEMBERS_OWNER),
      '/api/boards/board-a/widgets/cycle-lead-time': () =>
        jsonResponse({
          cards: [
            { cardId: 'card-a', title: 'A', leadTimeDays: 4, cycleTimeDays: 3, doneAt: '2026-09-05T00:00:00Z' },
            { cardId: 'card-b', title: 'B', leadTimeDays: 2, cycleTimeDays: null, doneAt: '2026-09-03T00:00:00Z' },
          ],
          leadTime: { p50: 3, p85: 4, p95: 4 },
          cycleTime: { p50: 3, p85: 3, p95: 3 },
          excludedCycleTimeCount: 1,
        }),
    });

    render(
      <MemoryRouter initialEntries={['/boards/board-a']}>
        <App />
      </MemoryRouter>,
    );

    const item = await screen.findByTestId('canvas-item-item-1');
    await waitFor(() =>
      expect(calls.some((c) => c.url.endsWith('/api/boards/board-a/widgets/cycle-lead-time'))).toBe(true),
    );

    await waitFor(() => expect(within(item).getByText('A')).toBeInTheDocument());
    expect(within(item).getByText('B')).toBeInTheDocument();
    expect(within(item).getByText('無')).toBeInTheDocument();
    expect(within(item).getByText('1')).toBeInTheDocument();
  });
});
