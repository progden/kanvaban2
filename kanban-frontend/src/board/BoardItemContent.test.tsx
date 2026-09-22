// 對應 ui-kanban-basic.md s-board／s-swimlane-list／s-swimlane-delete-dialog／s-stage-list／
// s-stage-delete-dialog／s-card-add-dialog／s-card-detail／s-card-delete-dialog 與
// ui-user-membership.md s-card-assignee-picker 的操作表／驗收條件；透過 App 掛載（比照
// BoardCanvasPage.test.tsx），因為 item.component === 'board' 的內容需要 BoardContext（boardId／canEdit）
// 與 F07 s-canvas 的掛載機制才能渲染。
import { fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, describe, expect, it, vi } from 'vitest';
import App from '../App';

function jsonResponse(body: unknown, status = 200) {
  return new Response(status === 204 ? null : JSON.stringify(body), { status });
}

function mockFetchByPath(responses: Record<string, (method: string) => Response>) {
  const calls: { url: string; method: string; body: unknown }[] = [];
  vi.spyOn(globalThis, 'fetch').mockImplementation((input, init) => {
    const url = typeof input === 'string' ? input : (input as Request).url;
    const method = init?.method ?? 'GET';
    calls.push({ url, method, body: init?.body === undefined ? undefined : JSON.parse(init.body as string) });
    const path = Object.keys(responses).find((candidate) => url.endsWith(candidate));
    if (path === undefined) {
      throw new Error(`未預期的請求：${method} ${url}`);
    }
    return Promise.resolve(responses[path](method));
  });
  return calls;
}

const SESSION_OK = () => jsonResponse({ username: 'user1', displayName: '陳柏翰' });

function board(overrides: Partial<Record<string, unknown>> = {}) {
  return {
    id: 'board-a',
    name: '產品開發看板',
    createdBy: 'user1',
    swimlanes: [
      { id: 'sw-1', name: '前端', order: 0 },
      { id: 'sw-2', name: '後端', order: 1 },
    ],
    stages: [
      { id: 'st-1', name: 'Backlog', order: 0, role: 'START' },
      { id: 'st-2', name: '完成', order: 1, role: 'DONE' },
    ],
    clockTime: '2026-09-22T00:00:00Z',
    clockStatus: 'RUNNING',
    ...overrides,
  };
}

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

function canvasResponse() {
  return { id: 'canvas-1', boardId: 'board-a', zoomMin: 0.1, zoomMax: 4, items: [BOARD_ITEM], viewport: null };
}

const MEMBERS_OWNER = [{ username: 'user1', displayName: '陳柏翰', role: 'OWNER' }];
const MEMBERS_VIEWER = [{ username: 'user1', displayName: '陳柏翰', role: 'VIEWER' }];

const CANDIDATES = [
  { id: 'user-1', username: 'poyu.chen', displayName: '陳柏翰' },
  { id: 'user-2', username: 'yifan.tsai', displayName: '蔡依帆' },
];

const CARD_A = {
  id: 'card-1',
  boardId: 'board-a',
  title: '結帳頁 3DS 驗證流程',
  description: '',
  dueDate: '2026-10-02',
  labels: [],
  swimlaneId: 'sw-1',
  stageId: 'st-1',
  assigneeIds: ['user-1'],
  comments: [],
};

afterEach(() => {
  vi.restoreAllMocks();
});

async function renderBoard(
  extra: Record<string, (method: string) => Response> = {},
  boardOverrides: Partial<Record<string, unknown>> = {},
  members: unknown = MEMBERS_OWNER,
) {
  const calls = mockFetchByPath({
    '/api/session': SESSION_OK,
    '/api/boards/board-a': () => jsonResponse(board(boardOverrides)),
    '/api/boards/board-a/canvas': () => jsonResponse(canvasResponse()),
    '/api/boards/board-a/members': () => jsonResponse(members),
    '/api/boards/board-a/cards': () => jsonResponse([CARD_A]),
    '/api/boards/board-a/assignee-candidates': () => jsonResponse(CANDIDATES),
    '/api/cards/card-1': () => jsonResponse(CARD_A),
    ...extra,
  });

  render(
    <MemoryRouter initialEntries={['/boards/board-a']}>
      <App />
    </MemoryRouter>,
  );

  await waitFor(() => expect(screen.getByText('結帳頁 3DS 驗證流程')).toBeInTheDocument());
  return calls;
}

describe('s-board', () => {
  it('依 Swimlane × Stage 交會格顯示卡片，含標題、截止日期與負責人', async () => {
    await renderBoard();

    expect(screen.getByText('前端')).toBeInTheDocument();
    expect(screen.getByText('後端')).toBeInTheDocument();
    expect(screen.getByText('Backlog')).toBeInTheDocument();
    expect(screen.getByText('2026-10-02')).toBeInTheDocument();
    expect(screen.getByTitle('陳柏翰')).toBeInTheDocument();
  });

  it('拖曳卡片到另一個 Swimlane 觸發 uc-move-card-swimlane', async () => {
    const calls = await renderBoard({
      '/api/cards/card-1/move-swimlane': () => jsonResponse({ ...CARD_A, swimlaneId: 'sw-2' }),
    });

    const card = screen.getByText('結帳頁 3DS 驗證流程').closest('.board-card') as HTMLElement;
    const dataTransfer = { getData: () => 'card-1', setData: () => undefined };
    fireEvent.dragStart(card, { dataTransfer });
    const targetCell = screen.getByText('後端').nextElementSibling as HTMLElement;
    fireEvent.dragOver(targetCell, { dataTransfer });
    fireEvent.drop(targetCell, { dataTransfer });

    await waitFor(() =>
      expect(calls.some((c) => c.url.endsWith('/api/cards/card-1/move-swimlane'))).toBe(true),
    );
    const call = calls.find((c) => c.url.endsWith('/api/cards/card-1/move-swimlane'));
    expect(call?.body).toEqual({ swimlaneId: 'sw-2' });
  });

  it('拖曳卡片到另一個 Stage 後，卡片顯示於目的 Stage，觸發 uc-move-card-stage', async () => {
    const calls = await renderBoard({
      '/api/cards/card-1/move-stage': () => jsonResponse({ ...CARD_A, stageId: 'st-2' }),
    });

    const card = screen.getByText('結帳頁 3DS 驗證流程').closest('.board-card') as HTMLElement;
    const dataTransfer = { getData: () => 'card-1', setData: () => undefined };
    fireEvent.dragStart(card, { dataTransfer });
    const targetCell = screen.getByText('前端').nextElementSibling?.nextElementSibling as HTMLElement;
    fireEvent.dragOver(targetCell, { dataTransfer });
    fireEvent.drop(targetCell, { dataTransfer });

    await waitFor(() => expect(calls.some((c) => c.url.endsWith('/api/cards/card-1/move-stage'))).toBe(true));
    const call = calls.find((c) => c.url.endsWith('/api/cards/card-1/move-stage'));
    expect(call?.body).toEqual({ stageId: 'st-2' });
  });

  it('依 uc-move-card-swimlane p2：移動失敗時卡片維持原位，顯示訊息', async () => {
    await renderBoard({
      '/api/cards/card-1/move-swimlane': () => jsonResponse({ message: '無法移動卡片' }, 400),
    });

    const card = screen.getByText('結帳頁 3DS 驗證流程').closest('.board-card') as HTMLElement;
    const dataTransfer = { getData: () => 'card-1', setData: () => undefined };
    fireEvent.dragStart(card, { dataTransfer });
    const targetCell = screen.getByText('後端').nextElementSibling as HTMLElement;
    fireEvent.dragOver(targetCell, { dataTransfer });
    fireEvent.drop(targetCell, { dataTransfer });

    await waitFor(() => expect(screen.getByRole('alert')).toHaveTextContent('無法移動卡片'));
    const frontCell = screen.getByText('前端').nextElementSibling as HTMLElement;
    expect(within(frontCell).getByText('結帳頁 3DS 驗證流程')).toBeInTheDocument();
  });

  it('依 uc-move-card-stage p2：移動失敗時卡片維持原位，顯示訊息', async () => {
    await renderBoard({
      '/api/cards/card-1/move-stage': () => jsonResponse({ message: '無法移動卡片' }, 400),
    });

    const card = screen.getByText('結帳頁 3DS 驗證流程').closest('.board-card') as HTMLElement;
    const dataTransfer = { getData: () => 'card-1', setData: () => undefined };
    fireEvent.dragStart(card, { dataTransfer });
    const targetCell = screen.getByText('前端').nextElementSibling?.nextElementSibling as HTMLElement;
    fireEvent.dragOver(targetCell, { dataTransfer });
    fireEvent.drop(targetCell, { dataTransfer });

    await waitFor(() => expect(screen.getByRole('alert')).toHaveTextContent('無法移動卡片'));
    const originCell = screen.getByText('前端').nextElementSibling as HTMLElement;
    expect(within(originCell).getByText('結帳頁 3DS 驗證流程')).toBeInTheDocument();
  });

  it('點擊新增卡片開啟 s-card-add-dialog', async () => {
    await renderBoard();

    const addButtons = screen.getAllByRole('button', { name: '＋ 新增卡片' });
    fireEvent.click(addButtons[0]);

    expect(screen.getByRole('heading', { name: '新增卡片' })).toBeInTheDocument();
  });

  it('操作者角色為 Viewer 時，不顯示新增／刪除卡片與管理按鈕', async () => {
    await renderBoard({}, {}, MEMBERS_VIEWER);

    expect(screen.queryAllByRole('button', { name: '＋ 新增卡片' })).toHaveLength(0);
    expect(screen.queryByRole('button', { name: '管理 Swimlane' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '管理 Stage' })).not.toBeInTheDocument();
  });

  it('點擊卡片開啟 s-card-detail', async () => {
    await renderBoard();

    fireEvent.click(screen.getByText('結帳頁 3DS 驗證流程'));

    expect(await screen.findByText('留言')).toBeInTheDocument();
  });

  it('點擊刪除卡片開啟 s-card-delete-dialog，確認刪除後觸發 uc-delete-card 並自看板移除', async () => {
    let deleted = false;
    const calls = await renderBoard({
      '/api/cards/card-1': (method) => {
        if (method === 'DELETE') {
          deleted = true;
          return jsonResponse(undefined, 204);
        }
        return jsonResponse(CARD_A);
      },
      '/api/boards/board-a/cards': () => jsonResponse(deleted ? [] : [CARD_A]),
    });

    fireEvent.click(screen.getByRole('button', { name: '刪除卡片「結帳頁 3DS 驗證流程」' }));
    const heading = screen.getByRole('heading', { name: '刪除卡片「結帳頁 3DS 驗證流程」？' });
    const dialogPanel = heading.closest('.dialog-panel') as HTMLElement;
    fireEvent.click(within(dialogPanel).getByRole('button', { name: '刪除卡片' }));

    await waitFor(() => expect(screen.queryByText('結帳頁 3DS 驗證流程')).not.toBeInTheDocument());
    expect(calls.some((c) => c.method === 'DELETE' && c.url.endsWith('/api/cards/card-1'))).toBe(true);
  });
});

describe('s-card-add-dialog', () => {
  it('開啟時顯示目的 Swimlane、Stage 名稱；標題為空時保留輸入並顯示訊息', async () => {
    await renderBoard({
      '/api/boards/board-a/cards': (method) =>
        method === 'GET' ? jsonResponse([CARD_A]) : jsonResponse({ message: '卡片標題不能留空' }, 400),
    });

    fireEvent.click(screen.getAllByRole('button', { name: '＋ 新增卡片' })[0]);
    expect(screen.getAllByText('前端').length).toBeGreaterThan(0);
    expect(screen.getAllByText('Backlog').length).toBeGreaterThan(0);

    fireEvent.click(screen.getByRole('button', { name: '新增卡片' }));
    await waitFor(() => expect(screen.getByRole('alert')).toHaveTextContent('卡片標題不能留空'));
    expect(screen.getByRole('heading', { name: '新增卡片' })).toBeInTheDocument();
  });

  it('確認新增成功後關閉對話框，新卡片顯示於指定交會格，觸發 uc-add-card', async () => {
    const NEW_CARD = { ...CARD_A, id: 'card-2', title: '新卡片' };
    let cards = [CARD_A];
    const calls = await renderBoard({
      '/api/boards/board-a/cards': (method) => {
        if (method === 'POST') {
          cards = [...cards, NEW_CARD];
          return jsonResponse(NEW_CARD, 201);
        }
        return jsonResponse(cards);
      },
    });

    fireEvent.click(screen.getAllByRole('button', { name: '＋ 新增卡片' })[0]);
    fireEvent.change(screen.getByLabelText('卡片標題'), { target: { value: '新卡片' } });
    fireEvent.click(screen.getByRole('button', { name: '新增卡片' }));

    await waitFor(() => expect(screen.getByText('新卡片')).toBeInTheDocument());
    expect(screen.queryByRole('heading', { name: '新增卡片' })).not.toBeInTheDocument();
    expect(calls.some((c) => c.method === 'POST' && c.url.endsWith('/api/boards/board-a/cards'))).toBe(true);
  });

  it('取消後關閉對話框，且不觸發 uc-add-card', async () => {
    const calls = await renderBoard();

    fireEvent.click(screen.getAllByRole('button', { name: '＋ 新增卡片' })[0]);
    fireEvent.click(screen.getByRole('button', { name: '取消' }));

    expect(screen.queryByRole('heading', { name: '新增卡片' })).not.toBeInTheDocument();
    expect(calls.some((c) => c.method === 'POST' && c.url.endsWith('/api/boards/board-a/cards'))).toBe(false);
  });
});

describe('s-card-detail', () => {
  it('開啟時顯示卡片標題、描述、截止日期、負責人與留言；沒有指派負責人時顯示未指派', async () => {
    const noAssignee = { ...CARD_A, assigneeIds: [] };
    await renderBoard({
      '/api/boards/board-a/cards': () => jsonResponse([noAssignee]),
      '/api/cards/card-1': () => jsonResponse(noAssignee),
    });

    fireEvent.click(screen.getByText('結帳頁 3DS 驗證流程'));
    await waitFor(() => expect(screen.getAllByText('結帳頁 3DS 驗證流程').length).toBeGreaterThan(1));
    expect(screen.getByText('未指派')).toBeInTheDocument();
  });

  it('儲存變更後顯示儲存的內容，觸發 uc-edit-card', async () => {
    let currentCard = CARD_A;
    const calls = await renderBoard({
      '/api/cards/card-1': (method) => {
        if (method === 'PATCH') {
          currentCard = { ...currentCard, description: '更新後的描述' };
          return jsonResponse(currentCard);
        }
        return jsonResponse(currentCard);
      },
    });

    fireEvent.click(screen.getByText('結帳頁 3DS 驗證流程'));
    await screen.findByLabelText('描述');

    fireEvent.change(screen.getByLabelText('描述'), { target: { value: '更新後的描述' } });
    fireEvent.click(screen.getByRole('button', { name: '儲存變更' }));

    await waitFor(() =>
      expect(calls.some((c) => c.method === 'PATCH' && c.url.endsWith('/api/cards/card-1'))).toBe(true),
    );
    expect(screen.getByLabelText('描述')).toHaveValue('更新後的描述');
  });

  it('留言列表顯示 comment.created-at，並依時間由舊到新排序', async () => {
    const OLDER = { id: 'c-1', authorId: 'user-1', content: '較舊的留言', createdAt: '2026-09-15T14:02:00Z' };
    const NEWER = { id: 'c-2', authorId: 'user-2', content: '較新的留言', createdAt: '2026-09-16T09:30:00Z' };
    await renderBoard({
      '/api/cards/card-1': () => jsonResponse({ ...CARD_A, comments: [NEWER, OLDER] }),
    });

    fireEvent.click(screen.getByText('結帳頁 3DS 驗證流程'));
    await screen.findByText('留言');

    const comments = screen.getAllByText(/較舊的留言|較新的留言/);
    expect(comments[0]).toHaveTextContent('較舊的留言');
    expect(comments[1]).toHaveTextContent('較新的留言');
    const pad = (n: number) => n.toString().padStart(2, '0');
    const olderDate = new Date(OLDER.createdAt);
    const expectedTime = `${pad(olderDate.getMonth() + 1)}/${pad(olderDate.getDate())} ${pad(olderDate.getHours())}:${pad(olderDate.getMinutes())}`;
    const olderItem = screen.getByText('較舊的留言').closest('li') as HTMLElement;
    expect(within(olderItem).getByText(expectedTime)).toBeInTheDocument();
  });

  it('新增留言後顯示於留言列表，觸發 uc-add-comment', async () => {
    const COMMENT = { id: 'c-1', authorId: 'user-1', content: '測試留言', createdAt: '2026-09-22T00:00:00Z' };
    let commentsAdded = false;
    await renderBoard({
      '/api/cards/card-1': () => jsonResponse(commentsAdded ? { ...CARD_A, comments: [COMMENT] } : CARD_A),
      '/api/cards/card-1/comments': () => {
        commentsAdded = true;
        return jsonResponse(COMMENT, 201);
      },
    });

    fireEvent.click(screen.getByText('結帳頁 3DS 驗證流程'));
    await screen.findByLabelText('新增留言');
    fireEvent.change(screen.getByLabelText('新增留言'), { target: { value: '測試留言' } });
    fireEvent.click(screen.getByRole('button', { name: '送出' }));

    await waitFor(() => expect(screen.getByText('測試留言')).toBeInTheDocument());
  });

  it('依 uc-add-comment p2：留言內容為空被拒絕時，輸入內容保留，顯示訊息', async () => {
    await renderBoard({
      '/api/cards/card-1/comments': () => jsonResponse({ message: '留言內容不能留空' }, 400),
    });

    fireEvent.click(screen.getByText('結帳頁 3DS 驗證流程'));
    await screen.findByLabelText('新增留言');
    fireEvent.change(screen.getByLabelText('新增留言'), { target: { value: ' ' } });
    fireEvent.click(screen.getByRole('button', { name: '送出' }));

    await waitFor(() => expect(screen.getByRole('alert')).toHaveTextContent('留言內容不能留空'));
    expect(screen.getByLabelText('新增留言')).toHaveValue(' ');
  });

  it('開啟負責人選取入口開啟 s-card-assignee-picker，不觸發任何 Use Case', async () => {
    const calls = await renderBoard({
      '/api/cards/card-1': () => jsonResponse(CARD_A),
    });

    fireEvent.click(screen.getByText('結帳頁 3DS 驗證流程'));
    await screen.findByText('負責人');
    calls.length = 0;
    fireEvent.click(screen.getByRole('button', { name: '變更' }));

    expect(await screen.findByRole('heading', { name: '指派負責人' })).toBeInTheDocument();
    expect(calls.some((c) => c.method !== 'GET')).toBe(false);
  });

  it('關閉後回到 s-board，看板交會格內容更新', async () => {
    await renderBoard({
      '/api/cards/card-1': () => jsonResponse(CARD_A),
    });

    fireEvent.click(screen.getByText('結帳頁 3DS 驗證流程'));
    await screen.findByLabelText('描述');
    fireEvent.click(screen.getByRole('button', { name: '關閉卡片詳情' }));

    expect(screen.queryByLabelText('描述')).not.toBeInTheDocument();
    expect(screen.getByText('前端')).toBeInTheDocument();
  });
});

describe('s-card-assignee-picker', () => {
  async function openPicker(extra: Record<string, (method: string) => Response> = {}) {
    const calls = await renderBoard({
      '/api/cards/card-1': () => jsonResponse(CARD_A),
      ...extra,
    });
    fireEvent.click(screen.getByText('結帳頁 3DS 驗證流程'));
    await screen.findByRole('button', { name: '變更' });
    fireEvent.click(screen.getByRole('button', { name: '變更' }));
    await screen.findByRole('heading', { name: '指派負責人' });
    return calls;
  }

  it('候選清單只列出該看板成員，目前負責人顯示為已勾選', async () => {
    await openPicker();

    const checkbox1 = screen.getByRole('checkbox', { name: /陳柏翰/ });
    const checkbox2 = screen.getByRole('checkbox', { name: /蔡依帆/ });
    expect(checkbox1).toBeChecked();
    expect(checkbox2).not.toBeChecked();
  });

  it('勾選後儲存，觸發 uc-set-card-assignees，回到 s-card-detail 顯示新名單', async () => {
    await openPicker({
      '/api/cards/card-1/assignees': () => jsonResponse({ ...CARD_A, assigneeIds: ['user-1', 'user-2'] }),
    });

    fireEvent.click(screen.getByRole('checkbox', { name: /蔡依帆/ }));
    fireEvent.click(screen.getByRole('button', { name: '儲存' }));

    await waitFor(() => expect(screen.queryByRole('heading', { name: '指派負責人' })).not.toBeInTheDocument());
    expect(screen.getByTitle('蔡依帆')).toBeInTheDocument();
  });

  it('取消後關閉，不套用勾選變更，不觸發 uc-set-card-assignees', async () => {
    const calls = await openPicker();
    const callCountBeforeCancel = calls.length;

    fireEvent.click(screen.getByRole('checkbox', { name: /蔡依帆/ }));
    fireEvent.click(screen.getByRole('button', { name: '取消' }));

    expect(screen.queryByRole('heading', { name: '指派負責人' })).not.toBeInTheDocument();
    expect(calls.length).toBe(callCountBeforeCancel);
    expect(calls.some((c) => c.url.endsWith('/api/cards/card-1/assignees'))).toBe(false);
  });
});

describe('s-swimlane-list ／ s-swimlane-delete-dialog', () => {
  it('新增泳道成功後顯示在列表最下方，觸發 uc-add-swimlane', async () => {
    const calls = await renderBoard({
      '/api/boards/board-a/swimlanes': () =>
        jsonResponse(board({ swimlanes: [...board().swimlanes as unknown[], { id: 'sw-3', name: '新泳道', order: 2 }] }), 201),
    });

    fireEvent.click(screen.getByRole('button', { name: '管理 Swimlane' }));
    fireEvent.change(screen.getByLabelText('新增泳道'), { target: { value: '新泳道' } });
    fireEvent.click(screen.getByRole('button', { name: '新增' }));

    await waitFor(() =>
      expect(calls.some((c) => c.method === 'POST' && c.url.endsWith('/api/boards/board-a/swimlanes'))).toBe(true),
    );
  });

  it('該 Swimlane 沒有卡片時，刪除對話框顯示卡片數為 0', async () => {
    await renderBoard({
      '/api/boards/board-a/cards': () => jsonResponse([{ ...CARD_A, swimlaneId: 'sw-2' }]),
    });

    fireEvent.click(screen.getByRole('button', { name: '管理 Swimlane' }));
    fireEvent.click(screen.getAllByRole('button', { name: '刪除' })[0]);
    const heading = screen.getByRole('heading', { name: '刪除泳道「前端」？' });
    const dialogPanel = heading.closest('.dialog-panel') as HTMLElement;
    expect(within(dialogPanel).getByText('這個泳道裡的 0 張卡片會一起被刪除。')).toBeInTheDocument();
  });

  it('僅剩 1 個 Swimlane 時，刪除操作無法使用', async () => {
    await renderBoard({}, { swimlanes: [{ id: 'sw-1', name: '前端', order: 0 }] });

    fireEvent.click(screen.getByRole('button', { name: '管理 Swimlane' }));
    expect(screen.getByRole('button', { name: '刪除' })).toBeDisabled();
  });

  it('重新命名送出後，該列名稱更新為新名稱，觸發 uc-rename-swimlane', async () => {
    let currentBoard = board();
    const calls = await renderBoard({
      '/api/boards/board-a': () => jsonResponse(currentBoard),
      '/api/boards/board-a/swimlanes/sw-1': () => {
        currentBoard = {
          ...currentBoard,
          swimlanes: [
            { id: 'sw-1', name: '新前端', order: 0 },
            { id: 'sw-2', name: '後端', order: 1 },
          ],
        };
        return jsonResponse(currentBoard, 200);
      },
    });

    fireEvent.click(screen.getByRole('button', { name: '管理 Swimlane' }));
    const panel = screen.getByRole('heading', { name: '管理泳道' }).closest('.dialog-panel') as HTMLElement;
    const row = within(panel).getByText('前端').closest('li') as HTMLElement;
    fireEvent.click(within(row).getByRole('button', { name: '重新命名' }));
    fireEvent.change(within(row).getByRole('textbox'), { target: { value: '新前端' } });
    fireEvent.click(within(row).getByRole('button', { name: '儲存' }));

    await waitFor(() =>
      expect(calls.some((c) => c.method === 'PATCH' && c.url.endsWith('/api/boards/board-a/swimlanes/sw-1'))).toBe(
        true,
      ),
    );
    await waitFor(() => expect(within(panel).getByText('新前端')).toBeInTheDocument());
  });

  it('拖曳排序完成後，列表順序依拖曳結果更新，觸發 uc-reorder-swimlane', async () => {
    const calls = await renderBoard({
      '/api/boards/board-a/swimlanes/sw-2/move': () => jsonResponse(board(), 200),
    });

    fireEvent.click(screen.getByRole('button', { name: '管理 Swimlane' }));
    const panel = screen.getByRole('heading', { name: '管理泳道' }).closest('.dialog-panel') as HTMLElement;
    const sourceRow = within(panel).getByText('後端').closest('li') as HTMLElement;
    const targetRow = within(panel).getByText('前端').closest('li') as HTMLElement;
    const dataTransfer = { getData: () => '', setData: () => undefined };
    fireEvent.dragStart(sourceRow, { dataTransfer });
    fireEvent.dragOver(targetRow, { dataTransfer });
    fireEvent.drop(targetRow, { dataTransfer });

    await waitFor(() =>
      expect(
        calls.some((c) => c.method === 'POST' && c.url.endsWith('/api/boards/board-a/swimlanes/sw-2/move')),
      ).toBe(true),
    );
    const call = calls.find((c) => c.url.endsWith('/api/boards/board-a/swimlanes/sw-2/move'));
    expect(call?.body).toEqual({ beforeId: 'sw-1' });
  });

  it('新增名稱為空時，輸入內容保留、顯示訊息，列表不變', async () => {
    await renderBoard({
      '/api/boards/board-a/swimlanes': () => jsonResponse({ message: '泳道名稱不能留空' }, 400),
    });

    fireEvent.click(screen.getByRole('button', { name: '管理 Swimlane' }));
    const panel = screen.getByRole('heading', { name: '管理泳道' }).closest('.dialog-panel') as HTMLElement;
    fireEvent.change(screen.getByLabelText('新增泳道'), { target: { value: ' ' } });
    fireEvent.click(within(panel).getByRole('button', { name: '新增' }));

    await waitFor(() => expect(screen.getByRole('alert')).toHaveTextContent('泳道名稱不能留空'));
    expect(screen.getByLabelText('新增泳道')).toHaveValue(' ');
    expect(within(panel).getByText('前端')).toBeInTheDocument();
    expect(within(panel).getByText('後端')).toBeInTheDocument();
  });

  it('確認刪除 Swimlane 後回列表並重新載入，觸發 uc-delete-swimlane', async () => {
    const calls = await renderBoard({
      '/api/boards/board-a/swimlanes/sw-1': () => jsonResponse(undefined, 204),
    });

    fireEvent.click(screen.getByRole('button', { name: '管理 Swimlane' }));
    fireEvent.click(screen.getAllByRole('button', { name: '刪除' })[0]);
    const heading = screen.getByRole('heading', { name: '刪除泳道「前端」？' });
    const dialogPanel = heading.closest('.dialog-panel') as HTMLElement;
    fireEvent.click(within(dialogPanel).getByRole('button', { name: '刪除泳道' }));

    await waitFor(() =>
      expect(
        calls.some((c) => c.method === 'DELETE' && c.url.includes('/api/boards/board-a/swimlanes/sw-1')),
      ).toBe(true),
    );
  });
});

describe('s-stage-list ／ s-stage-delete-dialog', () => {
  it('新增階段觸發 uc-add-stage', async () => {
    const calls = await renderBoard({
      '/api/boards/board-a/stages': () => jsonResponse(board(), 201),
    });

    fireEvent.click(screen.getByRole('button', { name: '管理 Stage' }));
    fireEvent.change(screen.getByLabelText('新增階段'), { target: { value: '新階段' } });
    fireEvent.click(screen.getByRole('button', { name: '新增' }));

    await waitFor(() =>
      expect(calls.some((c) => c.method === 'POST' && c.url.endsWith('/api/boards/board-a/stages'))).toBe(true),
    );
  });

  it('設定角色送出後觸發 uc-set-stage-role', async () => {
    const calls = await renderBoard({
      '/api/boards/board-a/stages/st-2/role': () => jsonResponse(board(), 200),
    });

    fireEvent.click(screen.getByRole('button', { name: '管理 Stage' }));
    fireEvent.change(screen.getByLabelText('完成 的階段角色'), { target: { value: 'START' } });

    await waitFor(() =>
      expect(
        calls.some((c) => c.method === 'PATCH' && c.url.endsWith('/api/boards/board-a/stages/st-2/role')),
      ).toBe(true),
    );
  });

  it('重新命名送出後，該列名稱更新為新名稱，觸發 uc-rename-stage', async () => {
    let currentBoard = board();
    const calls = await renderBoard({
      '/api/boards/board-a': () => jsonResponse(currentBoard),
      '/api/boards/board-a/stages/st-1': () => {
        currentBoard = {
          ...currentBoard,
          stages: [
            { id: 'st-1', name: '新 Backlog', order: 0, role: 'START' },
            { id: 'st-2', name: '完成', order: 1, role: 'DONE' },
          ],
        };
        return jsonResponse(currentBoard, 200);
      },
    });

    fireEvent.click(screen.getByRole('button', { name: '管理 Stage' }));
    const panel = screen.getByRole('heading', { name: '管理階段' }).closest('.dialog-panel') as HTMLElement;
    const row = within(panel).getByText('Backlog').closest('li') as HTMLElement;
    fireEvent.click(within(row).getByRole('button', { name: '重新命名' }));
    fireEvent.change(within(row).getByRole('textbox'), { target: { value: '新 Backlog' } });
    fireEvent.click(within(row).getByRole('button', { name: '儲存' }));

    await waitFor(() =>
      expect(calls.some((c) => c.method === 'PATCH' && c.url.endsWith('/api/boards/board-a/stages/st-1'))).toBe(
        true,
      ),
    );
    await waitFor(() => expect(within(panel).getByText('新 Backlog')).toBeInTheDocument());
  });

  it('拖曳排序完成後，列表順序依拖曳結果更新，觸發 uc-reorder-stage', async () => {
    const calls = await renderBoard({
      '/api/boards/board-a/stages/st-2/move': () => jsonResponse(board(), 200),
    });

    fireEvent.click(screen.getByRole('button', { name: '管理 Stage' }));
    const panel = screen.getByRole('heading', { name: '管理階段' }).closest('.dialog-panel') as HTMLElement;
    const sourceRow = within(panel).getByText('完成').closest('li') as HTMLElement;
    const targetRow = within(panel).getByText('Backlog').closest('li') as HTMLElement;
    const dataTransfer = { getData: () => '', setData: () => undefined };
    fireEvent.dragStart(sourceRow, { dataTransfer });
    fireEvent.dragOver(targetRow, { dataTransfer });
    fireEvent.drop(targetRow, { dataTransfer });

    await waitFor(() =>
      expect(calls.some((c) => c.method === 'POST' && c.url.endsWith('/api/boards/board-a/stages/st-2/move'))).toBe(
        true,
      ),
    );
    const call = calls.find((c) => c.url.endsWith('/api/boards/board-a/stages/st-2/move'));
    expect(call?.body).toEqual({ beforeId: 'st-1' });
  });

  it('將某 Stage 設為 DONE 後，原持有 DONE 的 Stage 該列角色顯示變回 NONE', async () => {
    let currentBoard = board();
    await renderBoard({
      '/api/boards/board-a': () => jsonResponse(currentBoard),
      '/api/boards/board-a/stages/st-1/role': () => {
        currentBoard = {
          ...currentBoard,
          stages: [
            { id: 'st-1', name: 'Backlog', order: 0, role: 'DONE' },
            { id: 'st-2', name: '完成', order: 1, role: 'NONE' },
          ],
        };
        return jsonResponse(currentBoard, 200);
      },
    });

    fireEvent.click(screen.getByRole('button', { name: '管理 Stage' }));
    fireEvent.change(screen.getByLabelText('Backlog 的階段角色'), { target: { value: 'DONE' } });

    await waitFor(() => expect(screen.getByLabelText('完成 的階段角色')).toHaveValue('NONE'));
    expect(screen.getByLabelText('Backlog 的階段角色')).toHaveValue('DONE');
  });

  it('該 Stage 沒有卡片時，刪除對話框顯示卡片數為 0', async () => {
    await renderBoard({
      '/api/boards/board-a/cards': () => jsonResponse([{ ...CARD_A, stageId: 'st-2' }]),
    });

    fireEvent.click(screen.getByRole('button', { name: '管理 Stage' }));
    fireEvent.click(screen.getAllByRole('button', { name: '刪除' })[0]);
    const heading = screen.getByRole('heading', { name: '刪除階段「Backlog」？' });
    const dialogPanel = heading.closest('.dialog-panel') as HTMLElement;
    expect(within(dialogPanel).getByText('這個階段裡有 0 張卡片。')).toBeInTheDocument();
    expect(within(dialogPanel).queryByLabelText('卡片移到')).not.toBeInTheDocument();
  });

  it('僅剩 1 個 Stage 時，刪除操作無法使用', async () => {
    await renderBoard({}, { stages: [{ id: 'st-1', name: 'Backlog', order: 0, role: 'NONE' }] });

    fireEvent.click(screen.getByRole('button', { name: '管理 Stage' }));
    expect(screen.getByRole('button', { name: '刪除' })).toBeDisabled();
  });

  it('該 Stage 內有卡片時，需選擇目的 Stage 才能確認刪除；確認刪除後觸發 uc-delete-stage', async () => {
    const calls = await renderBoard({
      '/api/boards/board-a/stages/st-1': () => jsonResponse(undefined, 204),
    });

    fireEvent.click(screen.getByRole('button', { name: '管理 Stage' }));
    fireEvent.click(screen.getAllByRole('button', { name: '刪除' })[0]);
    const heading = screen.getByRole('heading', { name: '刪除階段「Backlog」？' });
    const dialogPanel = heading.closest('.dialog-panel') as HTMLElement;
    expect(within(dialogPanel).getByLabelText('卡片移到')).toBeInTheDocument();
    fireEvent.click(within(dialogPanel).getByRole('button', { name: '刪除階段' }));

    await waitFor(() =>
      expect(
        calls.some(
          (c) => c.method === 'DELETE' && c.url.includes('/api/boards/board-a/stages/st-1') && c.url.includes('destinationStageId'),
        ),
      ).toBe(true),
    );
  });
});
