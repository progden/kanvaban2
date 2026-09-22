// 對應 ui-user-membership.md s-member-management 驗收條件。直接掛載對話框（不經過 App／路由，
// 本畫面不依賴登入態以外的路由參數），比照 canvas/RemoveItemsDialog 等元件層級測試的簡潔度。
import { fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { MemberManagementDialog } from './MemberManagementDialog';

function jsonResponse(body: unknown, status = 200) {
  return new Response(status === 204 ? null : JSON.stringify(body), { status });
}

interface MockRoute {
  method: string;
  path: string;
  respond: () => Response;
}

function mockFetch(routes: MockRoute[]) {
  const calls: { method: string; url: string; body: unknown }[] = [];
  vi.spyOn(globalThis, 'fetch').mockImplementation((input, init) => {
    const url = typeof input === 'string' ? input : (input as Request).url;
    const method = init?.method ?? 'GET';
    calls.push({ method, url, body: init?.body === undefined ? undefined : JSON.parse(init.body as string) });
    const route = routes.find((r) => r.method === method && url.includes(r.path));
    if (route === undefined) {
      throw new Error(`未預期的請求：${method} ${url}`);
    }
    return Promise.resolve(route.respond());
  });
  return calls;
}

afterEach(() => {
  vi.restoreAllMocks();
});

const OWNER = { username: 'user1', displayName: '陳柏翰', role: 'OWNER' };
const MEMBER = { username: 'yating', displayName: '雅婷', role: 'MEMBER' };

describe('s-member-management', () => {
  it('Owner 邀請系統中存在且非現有成員的帳號後，成員清單新增一筆、成員數加 1', async () => {
    mockFetch([
      { method: 'GET', path: '/members', respond: () => jsonResponse([OWNER]) },
      { method: 'POST', path: '/members', respond: () => jsonResponse(null, 201) },
    ]);

    render(<MemberManagementDialog boardId="board-a" currentUsername="user1" onClose={() => {}} />);
    await screen.findByText('1 人');

    // GET 之後改回傳含新成員的清單，模擬邀請成功後重新整理
    vi.spyOn(globalThis, 'fetch').mockRestore();
    mockFetch([
      { method: 'GET', path: '/members', respond: () => jsonResponse([OWNER, MEMBER]) },
      { method: 'POST', path: '/members', respond: () => jsonResponse(null, 201) },
    ]);

    fireEvent.change(screen.getByLabelText('邀請成員'), { target: { value: 'yating' } });
    fireEvent.click(screen.getByRole('button', { name: '邀請' }));

    await screen.findByText('2 人');
    expect(screen.getByText('雅婷')).toBeInTheDocument();
  });

  it('邀請已是成員的帳號時，輸入內容保留、顯示訊息，成員清單不變', async () => {
    mockFetch([
      { method: 'GET', path: '/members', respond: () => jsonResponse([OWNER, MEMBER]) },
      {
        method: 'POST',
        path: '/members',
        respond: () => jsonResponse({ message: '此使用者已經是看板成員' }, 409),
      },
    ]);

    render(<MemberManagementDialog boardId="board-a" currentUsername="user1" onClose={() => {}} />);
    await screen.findByText('2 人');

    fireEvent.change(screen.getByLabelText('邀請成員'), { target: { value: 'yating' } });
    fireEvent.click(screen.getByRole('button', { name: '邀請' }));

    await screen.findByText('此使用者已經是看板成員');
    expect(screen.getByLabelText('邀請成員')).toHaveValue('yating');
    expect(screen.getByText('2 人')).toBeInTheDocument();
  });

  it('非 Owner 嘗試邀請成員時，顯示訊息，成員清單不變', async () => {
    const another = { username: 'jianhong', displayName: '建宏', role: 'MEMBER' };
    mockFetch([
      { method: 'GET', path: '/members', respond: () => jsonResponse([OWNER, MEMBER]) },
      {
        method: 'POST',
        path: '/members',
        respond: () => jsonResponse({ message: '只有 Owner 可以邀請成員' }, 403),
      },
    ]);

    render(<MemberManagementDialog boardId="board-a" currentUsername="yating" onClose={() => {}} />);
    await screen.findByText('2 人');

    fireEvent.change(screen.getByLabelText('邀請成員'), { target: { value: another.username } });
    fireEvent.click(screen.getByRole('button', { name: '邀請' }));

    await screen.findByText('只有 Owner 可以邀請成員');
    expect(screen.getByText('2 人')).toBeInTheDocument();
  });

  it('Owner 將 Member 升級為 Owner 後，確認過才觸發變更，該成員角色顯示為 Owner', async () => {
    mockFetch([
      { method: 'GET', path: '/members', respond: () => jsonResponse([OWNER, MEMBER]) },
      { method: 'PATCH', path: '/yating/role', respond: () => jsonResponse(null, 200) },
    ]);

    render(<MemberManagementDialog boardId="board-a" currentUsername="user1" onClose={() => {}} />);
    await screen.findByText('雅婷');

    const row = screen.getByText('雅婷').closest('li') as HTMLElement;
    fireEvent.click(within(row).getByRole('button', { name: '設為 Owner' }));
    await within(row).findByText(/這個動作沒辦法改回來/);

    // 確認前不應該已經觸發變更：改回傳更新後清單，確認後才會看到 Owner
    vi.spyOn(globalThis, 'fetch').mockRestore();
    mockFetch([
      { method: 'GET', path: '/members', respond: () => jsonResponse([OWNER, { ...MEMBER, role: 'OWNER' }]) },
      { method: 'PATCH', path: '/yating/role', respond: () => jsonResponse(null, 200) },
    ]);

    const confirmActions = within(row).getByText(/這個動作沒辦法改回來/).parentElement as HTMLElement;
    fireEvent.click(within(confirmActions).getByRole('button', { name: '設為 Owner' }));

    await waitFor(() => {
      const updatedRow = screen.getByText('雅婷').closest('li') as HTMLElement;
      expect(within(updatedRow).getByText('Owner')).toBeInTheDocument();
    });
  });

  it('非 Owner 嘗試變更成員角色時，顯示訊息，清單中該成員的角色顯示不變', async () => {
    mockFetch([
      { method: 'GET', path: '/members', respond: () => jsonResponse([OWNER, MEMBER]) },
      {
        method: 'PATCH',
        path: '/yating/role',
        respond: () => jsonResponse({ message: '只有 Owner 可以變更成員角色' }, 403),
      },
    ]);

    render(<MemberManagementDialog boardId="board-a" currentUsername="yating" onClose={() => {}} />);
    await screen.findByText('雅婷');

    const row = screen.getByText('雅婷').closest('li') as HTMLElement;
    fireEvent.click(within(row).getByRole('button', { name: '設為 Owner' }));
    const confirmActions = (await within(row).findByText(/這個動作沒辦法改回來/)).parentElement as HTMLElement;
    fireEvent.click(within(confirmActions).getByRole('button', { name: '設為 Owner' }));

    await within(row).findByText('只有 Owner 可以變更成員角色');
    expect(within(row).getByText('Member')).toBeInTheDocument();
  });

  it('移除非唯一 Owner 或 Member 後，先顯示確認，確認後該成員才自清單移除', async () => {
    mockFetch([
      { method: 'GET', path: '/members', respond: () => jsonResponse([OWNER, MEMBER]) },
      { method: 'DELETE', path: '/yating', respond: () => jsonResponse(null, 204) },
    ]);

    render(<MemberManagementDialog boardId="board-a" currentUsername="user1" onClose={() => {}} />);
    await screen.findByText('雅婷');

    const row = screen.getByText('雅婷').closest('li') as HTMLElement;
    fireEvent.click(within(row).getByRole('button', { name: '移除' }));
    await within(row).findByText('確定要移除 雅婷？');
    // 確認前不應該已經移除
    expect(screen.getByText('雅婷')).toBeInTheDocument();

    vi.spyOn(globalThis, 'fetch').mockRestore();
    mockFetch([
      { method: 'GET', path: '/members', respond: () => jsonResponse([OWNER]) },
      { method: 'DELETE', path: '/yating', respond: () => jsonResponse(null, 204) },
    ]);
    const confirmActions = screen.getByText('確定要移除 雅婷？').parentElement as HTMLElement;
    fireEvent.click(within(confirmActions).getByRole('button', { name: '移除' }));

    await waitFor(() => expect(screen.queryByText('雅婷')).not.toBeInTheDocument());
  });

  it('移除看板唯一 Owner 時，確認後顯示訊息，該成員仍留在清單中', async () => {
    mockFetch([
      { method: 'GET', path: '/members', respond: () => jsonResponse([OWNER]) },
      {
        method: 'DELETE',
        path: '/user1',
        respond: () => jsonResponse({ message: '看板至少需要保留一位 Owner' }, 409),
      },
    ]);

    render(<MemberManagementDialog boardId="board-a" currentUsername="user1" onClose={() => {}} />);
    const row = (await screen.findByText('陳柏翰')).closest('li') as HTMLElement;

    fireEvent.click(within(row).getByRole('button', { name: '移除' }));
    const confirmMessage = await within(row).findByText('確定要移除 陳柏翰？');
    const confirmActions = confirmMessage.parentElement as HTMLElement;
    fireEvent.click(within(confirmActions).getByRole('button', { name: '移除' }));

    await within(row).findByText('看板至少需要保留一位 Owner');
    expect(screen.getByText('陳柏翰')).toBeInTheDocument();
  });

  it('移除仍是卡片負責人的成員時，先顯示一般確認，確認後才依卡片張數要求再次確認，確認後才觸發移除', async () => {
    const cardMessage = '雅婷 仍是 2 張卡片的負責人，移除後這些卡片會變成未指派';
    mockFetch([
      { method: 'GET', path: '/members', respond: () => jsonResponse([OWNER, MEMBER]) },
      {
        method: 'DELETE',
        path: '/yating',
        respond: () => jsonResponse({ message: cardMessage }, 409),
      },
    ]);

    render(<MemberManagementDialog boardId="board-a" currentUsername="user1" onClose={() => {}} />);
    const row = (await screen.findByText('雅婷')).closest('li') as HTMLElement;

    fireEvent.click(within(row).getByRole('button', { name: '移除' }));
    const firstConfirm = await within(row).findByText('確定要移除 雅婷？');
    fireEvent.click(within(firstConfirm.parentElement as HTMLElement).getByRole('button', { name: '移除' }));

    await within(row).findByText(cardMessage);
    // 確認前不應該已經移除
    expect(screen.getByText('雅婷')).toBeInTheDocument();

    vi.spyOn(globalThis, 'fetch').mockRestore();
    mockFetch([
      { method: 'GET', path: '/members', respond: () => jsonResponse([OWNER]) },
      { method: 'DELETE', path: '/yating', respond: () => jsonResponse(null, 204) },
    ]);
    const confirmActions = screen.getByText(cardMessage).parentElement as HTMLElement;
    fireEvent.click(within(confirmActions).getByRole('button', { name: '移除' }));

    await waitFor(() => expect(screen.queryByText('雅婷')).not.toBeInTheDocument());
  });

  it('角色為 Viewer 的成員列沒有設為 Owner 按鈕', async () => {
    const viewer = { username: 'weichen', displayName: '偉辰', role: 'VIEWER' };
    mockFetch([{ method: 'GET', path: '/members', respond: () => jsonResponse([OWNER, viewer]) }]);

    render(<MemberManagementDialog boardId="board-a" currentUsername="user1" onClose={() => {}} />);
    const row = (await screen.findByText('偉辰')).closest('li') as HTMLElement;

    expect(within(row).queryByRole('button', { name: '設為 Owner' })).not.toBeInTheDocument();
  });

  it('r-board-member 看不到移除成員的操作', async () => {
    mockFetch([{ method: 'GET', path: '/members', respond: () => jsonResponse([OWNER, MEMBER]) }]);

    render(<MemberManagementDialog boardId="board-a" currentUsername="yating" onClose={() => {}} />);
    await screen.findByText('雅婷');

    expect(screen.queryByRole('button', { name: '移除' })).not.toBeInTheDocument();
  });
});
