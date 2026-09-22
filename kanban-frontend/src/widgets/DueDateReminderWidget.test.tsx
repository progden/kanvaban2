// 對應 ui-kanban-widgets.md s-duedate-reminder 操作表／驗收條件；驗證開啟畫面觸發
// uc-view-duedate-reminder、門檻天數為 0 或非正整數時輸入保留且不觸發，訊息與後端 fail-p1 一致。
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
  stages: [{ id: 'st-1', name: '待辦', order: 0, role: 'NONE' }],
  clockTime: '2026-09-22T00:00:00Z',
  clockStatus: 'RUNNING',
};

const WIDGET_ITEM = {
  id: 'item-1',
  component: 's-duedate-reminder',
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

describe('s-duedate-reminder', () => {
  it('開啟畫面時觸發 uc-view-duedate-reminder，顯示已逾期與即將到期清單', async () => {
    const calls = mockFetchByPath({
      '/api/session': SESSION_OK,
      '/api/boards/board-a': () => jsonResponse(BOARD_A),
      '/api/boards/board-a/canvas': () => jsonResponse(CANVAS_RESPONSE),
      '/api/boards/board-a/members': () => jsonResponse(MEMBERS_OWNER),
      '/api/boards/board-a/widgets/duedate-reminder?thresholdDays=7': () =>
        jsonResponse({
          overdue: [{ cardId: 'card-e', title: 'E', dueDate: '2026-09-10' }],
          upcoming: [{ cardId: 'card-f', title: 'F', dueDate: '2026-09-14' }],
        }),
    });

    render(
      <MemoryRouter initialEntries={['/boards/board-a']}>
        <App />
      </MemoryRouter>,
    );

    const item = await screen.findByTestId('canvas-item-item-1');
    await waitFor(() =>
      expect(
        calls.some((c) => c.url.endsWith('/api/boards/board-a/widgets/duedate-reminder?thresholdDays=7')),
      ).toBe(true),
    );

    await waitFor(() => expect(within(item).getByText('E')).toBeInTheDocument());
    expect(within(item).getByText('F')).toBeInTheDocument();
  });

  it('門檻天數為 0 時輸入保留、顯示訊息，且不再次觸發 uc-view-duedate-reminder', async () => {
    const calls = mockFetchByPath({
      '/api/session': SESSION_OK,
      '/api/boards/board-a': () => jsonResponse(BOARD_A),
      '/api/boards/board-a/canvas': () => jsonResponse(CANVAS_RESPONSE),
      '/api/boards/board-a/members': () => jsonResponse(MEMBERS_OWNER),
      '/api/boards/board-a/widgets/duedate-reminder?thresholdDays=7': () =>
        jsonResponse({ overdue: [], upcoming: [] }),
    });

    render(
      <MemoryRouter initialEntries={['/boards/board-a']}>
        <App />
      </MemoryRouter>,
    );

    const item = await screen.findByTestId('canvas-item-item-1');
    await waitFor(() => expect(within(item).getByText('目前沒有已逾期的卡片')).toBeInTheDocument());

    const initialCallCount = calls.filter((c) => c.url.includes('/widgets/duedate-reminder')).length;

    const input = within(item).getByLabelText('即將到期門檻天數');
    fireEvent.change(input, { target: { value: '0' } });
    fireEvent.click(within(item).getByRole('button', { name: '套用' }));

    await waitFor(() =>
      expect(within(item).getByRole('alert')).toHaveTextContent('門檻天數必須是 1 到 365 之間的正整數'),
    );
    expect((input as HTMLInputElement).value).toBe('0');
    expect(calls.filter((c) => c.url.includes('/widgets/duedate-reminder')).length).toBe(initialCallCount);
  });
});
