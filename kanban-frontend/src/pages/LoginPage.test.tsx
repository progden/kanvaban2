// 對應 ui-user-membership.md s-login 驗收條件；透過 App 掛載，因為登入表單依賴 AuthProvider。
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, describe, expect, it, vi } from 'vitest';
import App from '../App';

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

afterEach(() => {
  vi.restoreAllMocks();
});

describe('s-login', () => {
  it('帳號密碼正確時送出登入表單，觸發 uc-login，成功後 TopBar 顯示我的名稱', async () => {
    mockFetchByPath({
      '/api/session': () => new Response(JSON.stringify({ message: '尚未登入' }), { status: 401 }),
      '/api/login': () => new Response(JSON.stringify({ username: 'user1', displayName: 'user1' }), { status: 200 }),
    });

    render(
      <MemoryRouter initialEntries={['/login']}>
        <App />
      </MemoryRouter>,
    );

    await waitFor(() => expect(screen.getByRole('heading', { name: '登入' })).toBeInTheDocument());

    fireEvent.change(screen.getByLabelText('帳號 ID'), { target: { value: 'user1' } });
    fireEvent.change(screen.getByLabelText('密碼'), { target: { value: 'correct-password' } });
    fireEvent.click(screen.getByRole('button', { name: '登入' }));

    await waitFor(() => expect(screen.getByText('user1')).toBeInTheDocument());
    expect(screen.getByText(/Board 列表/)).toBeInTheDocument();
  });

  it('密碼與帳號不相符時送出登入表單，欄位保留、顯示訊息，停留本畫面', async () => {
    mockFetchByPath({
      '/api/session': () => new Response(JSON.stringify({ message: '尚未登入' }), { status: 401 }),
      '/api/login': () => new Response(JSON.stringify({ message: '帳號或密碼錯誤' }), { status: 401 }),
    });

    render(
      <MemoryRouter initialEntries={['/login']}>
        <App />
      </MemoryRouter>,
    );

    await waitFor(() => expect(screen.getByRole('heading', { name: '登入' })).toBeInTheDocument());

    fireEvent.change(screen.getByLabelText('帳號 ID'), { target: { value: 'user1' } });
    fireEvent.change(screen.getByLabelText('密碼'), { target: { value: 'wrong-password' } });
    fireEvent.click(screen.getByRole('button', { name: '登入' }));

    await waitFor(() => expect(screen.getByRole('alert')).toHaveTextContent('帳號或密碼錯誤'));
    expect(screen.getByLabelText('帳號 ID')).toHaveValue('user1');
    expect(screen.getByLabelText('密碼')).toHaveValue('wrong-password');
    expect(screen.getByRole('heading', { name: '登入' })).toBeInTheDocument();
  });

  it('帳號不存在時送出登入表單，顯示訊息 "帳號或密碼錯誤"', async () => {
    mockFetchByPath({
      '/api/session': () => new Response(JSON.stringify({ message: '尚未登入' }), { status: 401 }),
      '/api/login': () => new Response(JSON.stringify({ message: '帳號或密碼錯誤' }), { status: 401 }),
    });

    render(
      <MemoryRouter initialEntries={['/login']}>
        <App />
      </MemoryRouter>,
    );

    await waitFor(() => expect(screen.getByRole('heading', { name: '登入' })).toBeInTheDocument());

    fireEvent.change(screen.getByLabelText('帳號 ID'), { target: { value: 'ghost-user' } });
    fireEvent.change(screen.getByLabelText('密碼'), { target: { value: 'anything' } });
    fireEvent.click(screen.getByRole('button', { name: '登入' }));

    await waitFor(() => expect(screen.getByRole('alert')).toHaveTextContent('帳號或密碼錯誤'));
  });

  it('觸發前往建立帳號動作，開啟 s-signup，不觸發任何 Use Case', async () => {
    const calledPaths = mockFetchByPath({
      '/api/session': () => new Response(JSON.stringify({ message: '尚未登入' }), { status: 401 }),
    });

    render(
      <MemoryRouter initialEntries={['/login']}>
        <App />
      </MemoryRouter>,
    );

    await waitFor(() => expect(screen.getByRole('heading', { name: '登入' })).toBeInTheDocument());

    fireEvent.click(screen.getByRole('link', { name: '前往建立帳號' }));

    await waitFor(() => expect(screen.getByRole('heading', { name: '建立帳號' })).toBeInTheDocument());
    expect(calledPaths.some((path) => path.endsWith('/api/users') || path.endsWith('/api/login'))).toBe(false);
  });
});
