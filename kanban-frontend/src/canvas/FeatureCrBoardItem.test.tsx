// 對應 spec-feature-cr-board.md「Feature: Feature／CR 追蹤表」uc-view-feature-cr-board；
// 透過 App 掛載（比照 ActivityLogItem.test.tsx），因為畫面依賴 AuthProvider／ProtectedRoute
// 與路由參數 boardId，且掛載於 Canvas item.component === 'feature-cr-board'。
import { render, screen, waitFor, within } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, describe, expect, it, vi } from 'vitest';
import App from '../App';
import { registerItemComponent } from './itemComponentRegistry';
import { FeatureCrBoardItem } from './FeatureCrBoardItem';

registerItemComponent('feature-cr-board', FeatureCrBoardItem);

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

const FEATURE_CR_BOARD_ITEM = {
  id: 'item-1',
  component: 'feature-cr-board',
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
      jsonResponse({ id: 'canvas-1', boardId: 'board-a', zoomMin: 0.1, zoomMax: 4, items: [FEATURE_CR_BOARD_ITEM], viewport: null }),
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

describe('s-feature-cr-board', () => {
  it('顯示 Feature 的開發狀態', async () => {
    const calls = await renderCanvas({
      '/api/boards/board-a/feature-cr-board': () =>
        jsonResponse({ features: [{ featureId: 'F01', status: '已完成', crs: [] }], orphanCrIds: [], warnings: [] }),
    });

    expect(calls.some((c) => c.url.endsWith('/api/boards/board-a/feature-cr-board'))).toBe(true);

    const list = await screen.findByTestId('feature-cr-board-features');
    expect(within(list).getByText('F01')).toBeInTheDocument();
    expect(within(list).getByText('已完成')).toBeInTheDocument();
  });

  it('顯示 CR 影響哪個 Feature 以及其狀態', async () => {
    await renderCanvas({
      '/api/boards/board-a/feature-cr-board': () =>
        jsonResponse({
          features: [{ featureId: 'F01', status: '未開發', crs: [{ crId: 'CR-004', status: '開發中' }] }],
          orphanCrIds: [],
          warnings: [],
        }),
    });

    const list = await screen.findByTestId('feature-cr-board-features');
    expect(within(list).getByText('CR-004')).toBeInTheDocument();
    expect(within(list).getByText('開發中')).toBeInTheDocument();
  });

  it('CR 指到不存在的 Feature 時列為 orphan', async () => {
    await renderCanvas({
      '/api/boards/board-a/feature-cr-board': () =>
        jsonResponse({ features: [], orphanCrIds: ['CR-099'], warnings: [] }),
    });

    const orphans = await screen.findByTestId('feature-cr-board-orphans');
    expect(within(orphans).getByText('CR-099')).toBeInTheDocument();
  });

  it('標籤格式不合時顯示警告訊息', async () => {
    await renderCanvas({
      '/api/boards/board-a/feature-cr-board': () =>
        jsonResponse({
          features: [],
          orphanCrIds: [],
          warnings: ['卡片「格式錯誤的卡」帶有兩個 Feature 標籤，已忽略其 Feature 標籤'],
        }),
    });

    const warnings = await screen.findByTestId('feature-cr-board-warnings');
    expect(within(warnings).getByText(/帶有兩個 Feature 標籤/)).toBeInTheDocument();
  });

  it('沒有 Feature／CR 卡片時顯示空狀態', async () => {
    await renderCanvas({
      '/api/boards/board-a/feature-cr-board': () =>
        jsonResponse({ features: [], orphanCrIds: [], warnings: [] }),
    });

    expect(await screen.findByText('尚無 Feature／CR 卡片')).toBeInTheDocument();
  });

  it('載入失敗時顯示錯誤訊息', async () => {
    await renderCanvas({
      '/api/boards/board-a/feature-cr-board': () => jsonResponse({ message: '載入 Feature／CR 追蹤表失敗' }, 500),
    });

    expect(await screen.findByRole('alert')).toHaveTextContent('載入 Feature／CR 追蹤表失敗');
  });
});
