// 對應 ui-user-membership.md s-board-list／s-board-create-dialog／s-board-delete-dialog 驗收條件；
// 透過 App 掛載（比照 LoginPage.test.tsx），因為畫面依賴 AuthProvider 與 ProtectedRoute。
import { fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, describe, expect, it, vi } from 'vitest';
import App from '../App';

function jsonResponse(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), { status });
}

function mockFetchByPath(responses: Record<string, () => Response>) {
  const calledPaths: string[] = [];
  vi.spyOn(globalThis, 'fetch').mockImplementation((input) => {
    const url = typeof input === 'string' ? input : (input as Request).url;
    calledPaths.push(url);
    const path = Object.keys(responses).find((candidate) => url.endsWith(candidate));
    if (path === undefined) {
      throw new Error(`未預期的請求：${url}`);
    }
    return Promise.resolve(responses[path]());
  });
  return calledPaths;
}

const SESSION_OK = () => jsonResponse({ username: 'user1', displayName: '陳柏翰' });

const BOARD_A = {
  id: 'board-a',
  name: '產品開發看板',
  createdBy: 'user1',
  swimlanes: [{ id: 'sw-1', name: '預設泳道', order: 0 }],
  stages: [
    { id: 'st-1', name: '待辦', order: 0, role: 'NONE' },
    { id: 'st-2', name: '進行中', order: 1, role: 'NONE' },
    { id: 'st-3', name: '完成', order: 2, role: 'DONE' },
  ],
  clockTime: '2026-09-22T00:00:00Z',
  clockStatus: 'RUNNING',
};

afterEach(() => {
  vi.restoreAllMocks();
});

async function renderBoardList() {
  render(
    <MemoryRouter initialEntries={['/boards']}>
      <App />
    </MemoryRouter>,
  );
  await waitFor(() => expect(screen.getByRole('heading', { name: '我的看板' })).toBeInTheDocument());
}

describe('s-board-list', () => {
  it('顯示我是 Owner 或 Member 的 Board 列表，觸發 uc-view-board-list', async () => {
    const calledPaths = mockFetchByPath({
      '/api/session': SESSION_OK,
      '/api/boards': () => jsonResponse([BOARD_A]),
    });

    await renderBoardList();

    await waitFor(() => expect(screen.getByText('產品開發看板')).toBeInTheDocument());
    expect(calledPaths.some((path) => path.endsWith('/api/boards'))).toBe(true);
  });

  it('尚未擁有任何 Board 時，列表顯示空清單', async () => {
    mockFetchByPath({
      '/api/session': SESSION_OK,
      '/api/boards': () => jsonResponse([]),
    });

    await renderBoardList();

    await waitFor(() => expect(screen.getByText(/你還沒有看板/)).toBeInTheDocument());
  });

  it('觸發前往建立 Board 動作，開啟 s-board-create-dialog，不觸發任何 Use Case', async () => {
    const calledPaths = mockFetchByPath({
      '/api/session': SESSION_OK,
      '/api/boards': () => jsonResponse([]),
    });

    await renderBoardList();
    await waitFor(() => expect(screen.getByText(/你還沒有看板/)).toBeInTheDocument());

    fireEvent.click(screen.getByRole('button', { name: '建立看板' }));

    expect(screen.getByRole('heading', { name: '建立看板' })).toBeInTheDocument();
    expect(calledPaths.filter((path) => path.endsWith('/api/boards')).length).toBe(1);
  });

  it('觸發刪除 Board 動作，開啟 s-board-delete-dialog，不觸發 uc-delete-board', async () => {
    mockFetchByPath({
      '/api/session': SESSION_OK,
      '/api/boards': () => jsonResponse([BOARD_A]),
      '/card-count': () => jsonResponse({ cardCount: 28 }),
    });

    await renderBoardList();
    await waitFor(() => expect(screen.getByText('產品開發看板')).toBeInTheDocument());

    fireEvent.click(screen.getByRole('button', { name: '刪除' }));

    expect(screen.getByRole('heading', { name: '刪除看板「產品開發看板」？' })).toBeInTheDocument();
    expect(screen.getByText('產品開發看板')).toBeInTheDocument();
  });
});

describe('s-board-create-dialog', () => {
  it('輸入 Board 名稱後確認建立，觸發 uc-create-board，成功後關閉對話框且列表顯示新建立的 Board', async () => {
    let boards: (typeof BOARD_A)[] = [];
    vi.spyOn(globalThis, 'fetch').mockImplementation((input, init) => {
      const url = typeof input === 'string' ? input : (input as Request).url;
      const method = init?.method ?? 'GET';
      if (url.endsWith('/api/session')) {
        return Promise.resolve(SESSION_OK());
      }
      if (url.endsWith('/api/boards') && method === 'GET') {
        return Promise.resolve(jsonResponse(boards));
      }
      if (url.endsWith('/api/boards') && method === 'POST') {
        const newBoard = { ...BOARD_A, id: 'board-new', name: '行銷活動排程' };
        boards = [...boards, newBoard];
        return Promise.resolve(jsonResponse(newBoard, 201));
      }
      throw new Error(`未預期的請求：${method} ${url}`);
    });

    await renderBoardList();
    await waitFor(() => expect(screen.getByText(/你還沒有看板/)).toBeInTheDocument());

    fireEvent.click(screen.getByRole('button', { name: '建立看板' }));
    fireEvent.change(screen.getByLabelText('看板名稱'), { target: { value: '行銷活動排程' } });
    const dialog = screen.getByRole('heading', { name: '建立看板' }).closest('.dialog-panel') as HTMLElement;
    fireEvent.click(within(dialog).getByRole('button', { name: '建立看板' }));

    await waitFor(() =>
      expect(screen.queryByRole('heading', { name: '建立看板' })).not.toBeInTheDocument(),
    );
    expect(screen.getByText('行銷活動排程')).toBeInTheDocument();
  });

  it('取消後關閉對話框，且不觸發 uc-create-board', async () => {
    const calledPaths = mockFetchByPath({
      '/api/session': SESSION_OK,
      '/api/boards': () => jsonResponse([]),
    });

    await renderBoardList();
    await waitFor(() => expect(screen.getByText(/你還沒有看板/)).toBeInTheDocument());

    fireEvent.click(screen.getByRole('button', { name: '建立看板' }));
    fireEvent.click(screen.getByRole('button', { name: '取消' }));

    expect(screen.queryByRole('heading', { name: '建立看板' })).not.toBeInTheDocument();
    expect(calledPaths.filter((path) => path.endsWith('/api/boards')).length).toBe(1);
  });
});

describe('s-board-delete-dialog', () => {
  it('開啟時顯示該 Board 名稱、Swimlane 數、Stage 數與卡片數', async () => {
    mockFetchByPath({
      '/api/session': SESSION_OK,
      '/api/boards': () => jsonResponse([BOARD_A]),
      '/card-count': () => jsonResponse({ cardCount: 28 }),
    });

    await renderBoardList();
    await waitFor(() => expect(screen.getByText('產品開發看板')).toBeInTheDocument());
    fireEvent.click(screen.getByRole('button', { name: '刪除' }));

    const dialog = screen.getByRole('heading', { name: '刪除看板「產品開發看板」？' }).closest('.dialog-panel');
    expect(dialog).not.toBeNull();
    await waitFor(() => expect(within(dialog as HTMLElement).getByText('28')).toBeInTheDocument());
    expect(within(dialog as HTMLElement).getByText('1')).toBeInTheDocument();
    expect(within(dialog as HTMLElement).getByText('3')).toBeInTheDocument();
  });

  it('確認刪除後，觸發 uc-delete-board，導向 s-board-list，該 Board 自列表移除', async () => {
    let deleted = false;
    vi.spyOn(globalThis, 'fetch').mockImplementation((input, init) => {
      const url = typeof input === 'string' ? input : (input as Request).url;
      const method = init?.method ?? 'GET';
      if (url.endsWith('/api/session')) {
        return Promise.resolve(SESSION_OK());
      }
      if (url.endsWith('/api/boards')) {
        return Promise.resolve(jsonResponse(deleted ? [] : [BOARD_A]));
      }
      if (url.endsWith('/card-count')) {
        return Promise.resolve(jsonResponse({ cardCount: 28 }));
      }
      if (url.endsWith(`/api/boards/${BOARD_A.id}`) && method === 'DELETE') {
        deleted = true;
        return Promise.resolve(new Response(null, { status: 204 }));
      }
      throw new Error(`未預期的請求：${method} ${url}`);
    });

    await renderBoardList();
    await waitFor(() => expect(screen.getByText('產品開發看板')).toBeInTheDocument());
    fireEvent.click(screen.getByRole('button', { name: '刪除' }));
    await waitFor(() => expect(screen.getByText('28')).toBeInTheDocument());

    fireEvent.click(screen.getByRole('button', { name: '刪除看板' }));

    await waitFor(() =>
      expect(screen.queryByRole('heading', { name: '刪除看板「產品開發看板」？' })).not.toBeInTheDocument(),
    );
    expect(screen.queryByText('產品開發看板')).not.toBeInTheDocument();
  });

  it('取消後關閉對話框，且不觸發 uc-delete-board', async () => {
    mockFetchByPath({
      '/api/session': SESSION_OK,
      '/api/boards': () => jsonResponse([BOARD_A]),
      '/card-count': () => jsonResponse({ cardCount: 28 }),
    });

    await renderBoardList();
    await waitFor(() => expect(screen.getByText('產品開發看板')).toBeInTheDocument());
    fireEvent.click(screen.getByRole('button', { name: '刪除' }));
    await waitFor(() => expect(screen.getByText('28')).toBeInTheDocument());

    fireEvent.click(screen.getByRole('button', { name: '取消' }));

    expect(screen.queryByRole('heading', { name: '刪除看板「產品開發看板」？' })).not.toBeInTheDocument();
    expect(screen.getByText('產品開發看板')).toBeInTheDocument();
  });
});
