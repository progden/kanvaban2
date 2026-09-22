// 對應 ui-workload.md s-workload-dashboard 操作表／驗收條件。
// 獨立掛載測試，比照 BoardClockControl.test.tsx 的最小掛載點設計，useAuth 直接 mock 掉。
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import type { ReactElement } from 'react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { BoardContext } from '../board/BoardContext';
import { WorkloadDashboard } from './WorkloadDashboard';

function renderWithBoardContext(ui: ReactElement) {
  return render(
    <BoardContext.Provider
      value={{
        boardId: 'board-a',
        canEdit: true,
        requestedCardId: null,
        requestCardDetail: () => {},
        clearRequestedCardDetail: () => {},
      }}
    >
      {ui}
    </BoardContext.Provider>,
  );
}

vi.mock('../auth/useAuth', () => ({
  useAuth: () => ({ username: 'user1', displayName: '雅婷', status: 'authenticated' }),
}));

function jsonResponse(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), { status });
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

const WORKLOAD = {
  members: [
    { userId: 'u1', username: 'user1', displayName: '雅婷', cardCount: 3 },
    { userId: 'u2', username: 'user2', displayName: '志明', cardCount: 1 },
  ],
  unassignedCount: 2,
};

const MEMBERS_MEMBER = [
  { username: 'user1', displayName: '雅婷', role: 'MEMBER' },
  { username: 'user2', displayName: '志明', role: 'MEMBER' },
];

const MEMBERS_VIEWER = [{ username: 'user1', displayName: '雅婷', role: 'VIEWER' }];

const PROPS = { itemId: 'item-1', component: 's-workload-dashboard', width: 300, height: 200, boardId: 'board-a' };

afterEach(() => {
  vi.restoreAllMocks();
});

describe('s-workload-dashboard', () => {
  it('依 uc-view-workload 顯示各成員工作量與未指派卡片數量', async () => {
    mockFetchByPath({
      '/api/boards/board-a/workload': () => jsonResponse(WORKLOAD),
      '/api/boards/board-a/members': () => jsonResponse(MEMBERS_MEMBER),
    });

    renderWithBoardContext(<WorkloadDashboard {...PROPS} />);

    await waitFor(() => expect(screen.getByTestId('workload-count-u1')).toHaveTextContent('3'));
    expect(screen.getByTestId('workload-count-u2')).toHaveTextContent('1');
    expect(screen.getByTestId('workload-unassigned-count')).toHaveTextContent('2');
  });

  it('開啟時觸發 uc-view-workload（呼叫工作量表 API）', async () => {
    const calls = mockFetchByPath({
      '/api/boards/board-a/workload': () => jsonResponse(WORKLOAD),
      '/api/boards/board-a/members': () => jsonResponse(MEMBERS_MEMBER),
    });

    renderWithBoardContext(<WorkloadDashboard {...PROPS} />);

    await waitFor(() => expect(calls.some((c) => c.url.endsWith('/api/boards/board-a/workload'))).toBe(true));
  });

  it('點擊成員工作量數字開啟依負責人查詢卡片清單（s-cards-by-assignee）', async () => {
    mockFetchByPath({
      '/api/boards/board-a/workload': () => jsonResponse(WORKLOAD),
      '/api/boards/board-a/members': () => jsonResponse(MEMBERS_MEMBER),
      '/api/boards/board-a/cards/by-assignee/u1': () => jsonResponse([{ id: 'card-a', title: '卡片 A' }]),
    });

    renderWithBoardContext(<WorkloadDashboard {...PROPS} />);
    await waitFor(() => expect(screen.getByTestId('workload-count-u1')).toBeInTheDocument());

    fireEvent.click(screen.getByTestId('workload-count-u1'));

    expect(screen.getByText('雅婷 負責的卡片')).toBeInTheDocument();
    await waitFor(() => expect(screen.getByTestId('cards-by-assignee-list')).toHaveTextContent('卡片 A'));
  });

  it('成員角色為 Viewer 時，頭像不可拖曳（r-board-viewer 不能編輯任何內容）', async () => {
    mockFetchByPath({
      '/api/boards/board-a/workload': () => jsonResponse(WORKLOAD),
      '/api/boards/board-a/members': () => jsonResponse(MEMBERS_VIEWER),
    });

    renderWithBoardContext(<WorkloadDashboard {...PROPS} />);

    await waitFor(() => expect(screen.getByTestId('workload-avatar-u1')).toBeInTheDocument());
    expect(screen.getByTestId('workload-avatar-u1')).toHaveAttribute('draggable', 'false');
  });

  it('成員角色非 Viewer 時，頭像可拖曳', async () => {
    mockFetchByPath({
      '/api/boards/board-a/workload': () => jsonResponse(WORKLOAD),
      '/api/boards/board-a/members': () => jsonResponse(MEMBERS_MEMBER),
    });

    renderWithBoardContext(<WorkloadDashboard {...PROPS} />);

    await waitFor(() => expect(screen.getByTestId('workload-avatar-u1')).toBeInTheDocument());
    expect(screen.getByTestId('workload-avatar-u1')).toHaveAttribute('draggable', 'true');
  });
});
