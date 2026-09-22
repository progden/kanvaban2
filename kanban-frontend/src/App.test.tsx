import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, describe, expect, it, vi } from 'vitest';
import App from './App';

function mockFetchByPath(responses: Record<string, () => Response>) {
  vi.spyOn(globalThis, 'fetch').mockImplementation((input) => {
    const url = typeof input === 'string' ? input : (input as Request).url;
    const path = Object.keys(responses).find((candidate) => url.endsWith(candidate));
    if (path === undefined) {
      throw new Error(`未預期的請求：${url}`);
    }
    return Promise.resolve(responses[path]());
  });
}

afterEach(() => {
  vi.restoreAllMocks();
});

describe('App', () => {
  it('未登入時，根路徑導向登入畫面', async () => {
    mockFetchByPath({
      '/api/session': () => new Response(JSON.stringify({ message: '尚未登入' }), { status: 401 }),
    });

    render(
      <MemoryRouter initialEntries={['/']}>
        <App />
      </MemoryRouter>,
    );

    await waitFor(() => expect(screen.getByRole('heading', { name: '登入' })).toBeInTheDocument());
  });

  it('未登入時嘗試直接進入 Board 列表，會被導向登入畫面', async () => {
    mockFetchByPath({
      '/api/session': () => new Response(JSON.stringify({ message: '尚未登入' }), { status: 401 }),
    });

    render(
      <MemoryRouter initialEntries={['/boards']}>
        <App />
      </MemoryRouter>,
    );

    await waitFor(() => expect(screen.getByRole('heading', { name: '登入' })).toBeInTheDocument());
  });

  it('已登入時，Board 列表顯示 TopBar 顯示名字（而非帳號 ID）；登出後回到登入畫面', async () => {
    mockFetchByPath({
      '/api/session': () =>
        new Response(JSON.stringify({ username: 'alice', displayName: '小美' }), { status: 200 }),
      '/api/logout': () => new Response(null, { status: 204 }),
      '/api/boards': () => new Response(JSON.stringify([]), { status: 200 }),
    });

    render(
      <MemoryRouter initialEntries={['/boards']}>
        <App />
      </MemoryRouter>,
    );

    await waitFor(() => expect(screen.getByText('小美')).toBeInTheDocument());
    expect(screen.queryByText('alice')).not.toBeInTheDocument();
    expect(screen.getByRole('heading', { name: '我的看板' })).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: '登出' }));

    await waitFor(() => expect(screen.getByRole('heading', { name: '登入' })).toBeInTheDocument());
  });

  it('已登入時造訪登入畫面，會被導向 Board 列表', async () => {
    mockFetchByPath({
      '/api/session': () =>
        new Response(JSON.stringify({ username: 'alice', displayName: '小美' }), { status: 200 }),
      '/api/boards': () => new Response(JSON.stringify([]), { status: 200 }),
    });

    render(
      <MemoryRouter initialEntries={['/login']}>
        <App />
      </MemoryRouter>,
    );

    await waitFor(() => expect(screen.getByRole('heading', { name: '我的看板' })).toBeInTheDocument());
  });
});
