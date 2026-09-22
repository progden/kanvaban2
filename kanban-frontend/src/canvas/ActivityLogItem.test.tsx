// 對應 spec-user-membership.md「Feature: 檢視看板活動紀錄」uc-view-board-activity-log；
// 透過 App 掛載（比照 BoardCanvasPage.test.tsx），因為畫面依賴 AuthProvider／ProtectedRoute
// 與路由參數 boardId，且掛載於 Canvas item.component === 'activity-log'。
import { render, screen, waitFor, within } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, describe, expect, it, vi } from 'vitest';
import App from '../App';
import { registerItemComponent } from './itemComponentRegistry';
import { ActivityLogItem } from './ActivityLogItem';

registerItemComponent('activity-log', ActivityLogItem);

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

const ACTIVITY_LOG_ITEM = {
  id: 'item-1',
  component: 'activity-log',
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

const MEMBERS_OWNER = [{ username: 'user1', displayName: '陳柏翰', role: 'OWNER' }];

afterEach(() => {
  vi.restoreAllMocks();
});

async function renderCanvas(extra: Record<string, () => Response> = {}) {
  const calls = mockFetchByPath({
    '/api/session': SESSION_OK,
    '/api/boards/board-a': () => jsonResponse(BOARD_A),
    '/api/boards/board-a/canvas': () =>
      jsonResponse({ id: 'canvas-1', boardId: 'board-a', zoomMin: 0.1, zoomMax: 4, items: [ACTIVITY_LOG_ITEM], viewport: null }),
    '/api/boards/board-a/members': () => jsonResponse(MEMBERS_OWNER),
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

describe('s-activity-log', () => {
  it('依時間新到舊列出活動紀錄，顯示操作人與動作', async () => {
    const calls = await renderCanvas({
      '/api/boards/board-a/activity-log': () =>
        jsonResponse([
          { operatorUsername: 'user1', operatorDisplayName: '陳柏翰', action: '邀請 "雅婷" 加入看板', occurredAt: '2026-09-22T01:00:00Z' },
          { operatorUsername: 'user1', operatorDisplayName: '陳柏翰', action: '建立了這個 Board', occurredAt: '2026-09-22T00:00:00Z' },
        ]),
    });

    expect(calls.some((c) => c.url.endsWith('/api/boards/board-a/activity-log'))).toBe(true);

    const list = await screen.findByTestId('activity-log-list');
    const items = within(list).getAllByRole('listitem');
    expect(items).toHaveLength(2);
    expect(within(items[0]).getByText('邀請 "雅婷" 加入看板')).toBeInTheDocument();
    expect(within(items[0]).getByText('陳柏翰')).toBeInTheDocument();
    expect(within(items[1]).getByText('建立了這個 Board')).toBeInTheDocument();
  });

  it('沒有活動紀錄時顯示空狀態', async () => {
    await renderCanvas({
      '/api/boards/board-a/activity-log': () => jsonResponse([]),
    });

    expect(await screen.findByText('尚無活動紀錄')).toBeInTheDocument();
  });

  it('操作人查無使用者時顯示帳號或備援文字', async () => {
    await renderCanvas({
      '/api/boards/board-a/activity-log': () =>
        jsonResponse([{ operatorUsername: null, operatorDisplayName: null, action: '建立了這個 Board', occurredAt: '2026-09-22T00:00:00Z' }]),
    });

    const list = await screen.findByTestId('activity-log-list');
    expect(within(list).getByText('未知使用者')).toBeInTheDocument();
  });

  it('載入失敗時顯示錯誤訊息', async () => {
    await renderCanvas({
      '/api/boards/board-a/activity-log': () => jsonResponse({ message: '載入活動紀錄失敗' }, 500),
    });

    expect(await screen.findByRole('alert')).toHaveTextContent('載入活動紀錄失敗');
  });
});
