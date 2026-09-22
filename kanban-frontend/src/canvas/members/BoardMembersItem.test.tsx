// 對應 ui-canvas-layout.md s-canvas 操作表「於『看板成員』item 選擇加入成員」，
// 驗證掛載於畫布的「看板成員」item 顯示成員頭像，且點擊後開啟 s-member-management。
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { AuthContext } from '../../auth/auth-context';
import { BoardMembersItem } from './BoardMembersItem';

function jsonResponse(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), { status });
}

function mockFetch(responses: Record<string, () => Response>) {
  vi.spyOn(globalThis, 'fetch').mockImplementation((input) => {
    const url = typeof input === 'string' ? input : (input as Request).url;
    const path = Object.keys(responses).find((candidate) => url.includes(candidate));
    if (path === undefined) {
      throw new Error(`未預期的請求：${url}`);
    }
    return Promise.resolve(responses[path]());
  });
}

afterEach(() => {
  vi.restoreAllMocks();
});

const MEMBERS = [
  { username: 'user1', displayName: '陳柏翰', role: 'OWNER' },
  { username: 'yating', displayName: '雅婷', role: 'MEMBER' },
];

function renderItem() {
  return render(
    <AuthContext.Provider
      value={{
        status: 'authenticated',
        username: 'user1',
        displayName: '陳柏翰',
        login: vi.fn(),
        logout: vi.fn(),
      }}
    >
      <MemoryRouter initialEntries={['/boards/board-a']}>
        <Routes>
          <Route
            path="/boards/:boardId"
            element={<BoardMembersItem itemId="item-members" component="board-members" width={200} height={80} />}
          />
        </Routes>
      </MemoryRouter>
    </AuthContext.Provider>,
  );
}

describe('「看板成員」item', () => {
  it('顯示目前成員的頭像清單', async () => {
    mockFetch({ '/members': () => jsonResponse(MEMBERS) });
    renderItem();

    await waitFor(() => {
      expect(screen.getByText('陳')).toBeInTheDocument();
      expect(screen.getByText('雅')).toBeInTheDocument();
    });
  });

  it('點擊後開啟 s-member-management', async () => {
    mockFetch({ '/members': () => jsonResponse(MEMBERS) });
    renderItem();

    const trigger = await screen.findByRole('button', { name: '看板成員，選擇加入成員' });
    fireEvent.click(trigger);

    expect(await screen.findByRole('heading', { name: '看板成員' })).toBeInTheDocument();
  });
});
