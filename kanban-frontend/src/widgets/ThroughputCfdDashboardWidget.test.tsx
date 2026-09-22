// 對應 ui-kanban-widgets.md s-throughput-cfd-dashboard 操作表／驗收條件；驗證開啟畫面同時觸發
// uc-view-throughput（預設「日」）與 uc-view-cfd，切換單位時間重新觸發 uc-view-throughput。
import { fireEvent, render, screen, waitFor, within } from '@testing-library/react';
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
  stages: [
    { id: 'st-1', name: '待辦', order: 0, role: 'NONE' },
    { id: 'st-2', name: '進行中', order: 1, role: 'START' },
  ],
  clockTime: '2026-09-22T00:00:00Z',
  clockStatus: 'RUNNING',
};

const WIDGET_ITEM = {
  id: 'item-1',
  component: 's-throughput-cfd-dashboard',
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

describe('s-throughput-cfd-dashboard', () => {
  it('開啟畫面時以「日」為預設單位觸發 uc-view-throughput，並觸發 uc-view-cfd', async () => {
    const calls = mockFetchByPath({
      '/api/session': SESSION_OK,
      '/api/boards/board-a': () => jsonResponse(BOARD_A),
      '/api/boards/board-a/canvas': () => jsonResponse(CANVAS_RESPONSE),
      '/api/boards/board-a/members': () => jsonResponse(MEMBERS_OWNER),
      '/api/boards/board-a/widgets/throughput?unit=day': () =>
        jsonResponse({ periods: [{ periodStart: '2026-09-10', count: 2 }] }),
      '/api/boards/board-a/widgets/throughput?unit=week': () =>
        jsonResponse({ periods: [{ periodStart: '2026-09-07', count: 3 }] }),
      '/api/boards/board-a/widgets/cfd': () =>
        jsonResponse({ dataPoints: [{ date: '2026-09-10', countByStage: { 'st-1': 1, 'st-2': 2 } }] }),
    });

    render(
      <MemoryRouter initialEntries={['/boards/board-a']}>
        <App />
      </MemoryRouter>,
    );

    const item = await screen.findByTestId('canvas-item-item-1');
    await waitFor(() =>
      expect(calls.some((c) => c.url.endsWith('/api/boards/board-a/widgets/throughput?unit=day'))).toBe(true),
    );
    await waitFor(() => expect(calls.some((c) => c.url.endsWith('/api/boards/board-a/widgets/cfd'))).toBe(true));

    await waitFor(() => expect(within(item).getAllByText('2026-09-10').length).toBeGreaterThan(0));
    expect(within(item).getByText('待辦')).toBeInTheDocument();
    expect(within(item).getByText('進行中')).toBeInTheDocument();

    fireEvent.change(within(item).getByLabelText('單位時間'), { target: { value: 'week' } });

    await waitFor(() =>
      expect(calls.some((c) => c.url.endsWith('/api/boards/board-a/widgets/throughput?unit=week'))).toBe(true),
    );
    await waitFor(() => expect(within(item).getByText('2026-09-07')).toBeInTheDocument());
  });
});
