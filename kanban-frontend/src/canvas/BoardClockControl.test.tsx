// 對應 ui-board-clock.md s-board-clock-control 操作表／驗收條件。
// 獨立掛載測試（不經過 App／AuthProvider），比照 itemComponentRegistry 的最小掛載點設計，
// useAuth 直接 mock 掉，只驗證這個 item 內容本體的行為。
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { BoardClockControl } from './BoardClockControl';

vi.mock('../auth/useAuth', () => ({
  useAuth: () => ({ username: 'user1', displayName: '陳柏翰', status: 'authenticated' }),
}));

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

const BOARD = {
  id: 'board-a',
  name: '產品開發看板',
  createdBy: 'user1',
  swimlanes: [],
  stages: [],
  clockTime: '2026-09-12T11:00:00Z',
  clockStatus: 'REALTIME',
};

const MEMBERS_OWNER = [{ username: 'user1', displayName: '陳柏翰', role: 'OWNER' }];
const MEMBERS_MEMBER = [{ username: 'user1', displayName: '陳柏翰', role: 'MEMBER' }];

const PROPS = { itemId: 'item-1', component: 'board-clock-control', width: 300, height: 200, boardId: 'board-a' };

afterEach(() => {
  vi.restoreAllMocks();
});

describe('s-board-clock-control', () => {
  it('開啟時顯示看板時間目前值與狀態', async () => {
    mockFetchByPath({
      '/api/boards/board-a': () => jsonResponse(BOARD),
      '/api/boards/board-a/members': () => jsonResponse(MEMBERS_OWNER),
    });

    render(<BoardClockControl {...PROPS} />);

    await waitFor(() => expect(screen.getByTestId('clock-control-time')).toHaveTextContent('2026-09-12T11:00:00Z'));
    expect(screen.getByTestId('clock-control-status')).toHaveTextContent('REALTIME');
  });

  it('Owner 調整看板時間後，觸發 uc-adjust-board-clock 且畫面更新為調整後的時間', async () => {
    const calls = mockFetchByPath({
      '/api/boards/board-a': () => jsonResponse(BOARD),
      '/api/boards/board-a/members': () => jsonResponse(MEMBERS_OWNER),
      '/api/boards/board-a/clock': () => jsonResponse({ ...BOARD, clockTime: '2026-09-12T13:00:00Z' }),
    });

    render(<BoardClockControl {...PROPS} />);
    await waitFor(() => expect(screen.getByTestId('clock-control-time')).toBeInTheDocument());

    fireEvent.change(screen.getByLabelText('調整目標時間'), { target: { value: '2026-09-12T13:00:00' } });
    fireEvent.click(screen.getByRole('button', { name: '調整看板時間' }));

    await waitFor(() =>
      expect(screen.getByTestId('clock-control-time')).toHaveTextContent('2026-09-12T13:00:00Z'),
    );
    expect(calls.some((c) => c.url.endsWith('/api/boards/board-a/clock'))).toBe(true);
  });

  it('uc-adjust-board-clock 失敗時顯示訊息，看板時間顯示不變', async () => {
    mockFetchByPath({
      '/api/boards/board-a': () => jsonResponse(BOARD),
      '/api/boards/board-a/members': () => jsonResponse(MEMBERS_OWNER),
      '/api/boards/board-a/clock': () =>
        jsonResponse({ message: '只有 Owner 可以調整看板時間' }, 403),
    });

    render(<BoardClockControl {...PROPS} />);
    await waitFor(() => expect(screen.getByTestId('clock-control-time')).toBeInTheDocument());

    fireEvent.change(screen.getByLabelText('調整目標時間'), { target: { value: '2026-09-12T13:00:00' } });
    fireEvent.click(screen.getByRole('button', { name: '調整看板時間' }));

    await waitFor(() => expect(screen.getByRole('alert')).toHaveTextContent('只有 Owner 可以調整看板時間'));
    expect(screen.getByTestId('clock-control-time')).toHaveTextContent('2026-09-12T11:00:00Z');
  });

  it('暫停看板時間後，畫面顯示狀態為 PAUSED，觸發 uc-pause-resume-board-clock', async () => {
    const calls = mockFetchByPath({
      '/api/boards/board-a': () => jsonResponse(BOARD),
      '/api/boards/board-a/members': () => jsonResponse(MEMBERS_OWNER),
      '/api/boards/board-a/clock/pause': () => jsonResponse({ ...BOARD, clockStatus: 'PAUSED' }),
    });

    render(<BoardClockControl {...PROPS} />);
    await waitFor(() => expect(screen.getByTestId('clock-control-time')).toBeInTheDocument());

    fireEvent.click(screen.getByRole('button', { name: '暫停' }));

    await waitFor(() => expect(screen.getByTestId('clock-control-status')).toHaveTextContent('PAUSED'));
    expect(calls.some((c) => c.url.endsWith('/api/boards/board-a/clock/pause'))).toBe(true);
  });

  it('恢復看板時間後，畫面顯示狀態為 REALTIME，觸發 uc-pause-resume-board-clock', async () => {
    const calls = mockFetchByPath({
      '/api/boards/board-a': () => jsonResponse({ ...BOARD, clockStatus: 'PAUSED' }),
      '/api/boards/board-a/members': () => jsonResponse(MEMBERS_OWNER),
      '/api/boards/board-a/clock/resume': () => jsonResponse({ ...BOARD, clockStatus: 'REALTIME' }),
    });

    render(<BoardClockControl {...PROPS} />);
    await waitFor(() => expect(screen.getByRole('button', { name: '恢復' })).toBeInTheDocument());

    fireEvent.click(screen.getByRole('button', { name: '恢復' }));

    await waitFor(() => expect(screen.getByTestId('clock-control-status')).toHaveTextContent('REALTIME'));
    expect(calls.some((c) => c.url.endsWith('/api/boards/board-a/clock/resume'))).toBe(true);
  });

  it('非 Owner 只能檢視看板時間目前值與狀態，看不到調整／暫停操作', async () => {
    mockFetchByPath({
      '/api/boards/board-a': () => jsonResponse(BOARD),
      '/api/boards/board-a/members': () => jsonResponse(MEMBERS_MEMBER),
    });

    render(<BoardClockControl {...PROPS} />);

    await waitFor(() => expect(screen.getByTestId('clock-control-time')).toBeInTheDocument());
    expect(screen.queryByRole('button', { name: '調整看板時間' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '暫停' })).not.toBeInTheDocument();
  });
});
