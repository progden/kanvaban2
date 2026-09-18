// 對應 ui-user-membership.md s-signup 驗收條件。
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

async function renderSignup() {
  mockFetchByPath({
    '/api/session': () => new Response(JSON.stringify({ message: '尚未登入' }), { status: 401 }),
  });

  render(
    <MemoryRouter initialEntries={['/signup']}>
      <App />
    </MemoryRouter>,
  );

  await waitFor(() => expect(screen.getByRole('heading', { name: '建立帳號' })).toBeInTheDocument());
}

describe('s-signup', () => {
  it('帳號 ID 為空時確認建立帳號，輸入內容保留、顯示訊息，且不觸發 uc-create-user', async () => {
    const calledPaths = mockFetchByPath({
      '/api/session': () => new Response(JSON.stringify({ message: '尚未登入' }), { status: 401 }),
    });

    render(
      <MemoryRouter initialEntries={['/signup']}>
        <App />
      </MemoryRouter>,
    );
    await waitFor(() => expect(screen.getByRole('heading', { name: '建立帳號' })).toBeInTheDocument());

    fireEvent.change(screen.getByLabelText('密碼'), { target: { value: 'secret' } });
    fireEvent.click(screen.getByRole('button', { name: '確認建立帳號' }));

    await waitFor(() => expect(screen.getByRole('alert')).toHaveTextContent('使用者名稱不能為空'));
    expect(screen.getByLabelText('密碼')).toHaveValue('secret');
    expect(calledPaths.some((path) => path.endsWith('/api/users'))).toBe(false);
    expect(screen.getByRole('heading', { name: '建立帳號' })).toBeInTheDocument();
  });

  it('密碼長度超過 40 字時確認建立帳號，輸入內容保留、顯示訊息，且不觸發 uc-create-user', async () => {
    const calledPaths = mockFetchByPath({
      '/api/session': () => new Response(JSON.stringify({ message: '尚未登入' }), { status: 401 }),
    });

    render(
      <MemoryRouter initialEntries={['/signup']}>
        <App />
      </MemoryRouter>,
    );
    await waitFor(() => expect(screen.getByRole('heading', { name: '建立帳號' })).toBeInTheDocument());

    fireEvent.change(screen.getByLabelText('帳號 ID'), { target: { value: 'user4' } });
    fireEvent.change(screen.getByLabelText('密碼'), { target: { value: 'a'.repeat(41) } });
    fireEvent.click(screen.getByRole('button', { name: '確認建立帳號' }));

    await waitFor(() => expect(screen.getByRole('alert')).toHaveTextContent('密碼長度不可超過 40 個字'));
    expect(screen.getByLabelText('帳號 ID')).toHaveValue('user4');
    expect(calledPaths.some((path) => path.endsWith('/api/users'))).toBe(false);
  });

  it('帳號 ID 與系統中既有帳號重複時確認建立帳號，觸發 uc-create-user，輸入內容保留、顯示訊息', async () => {
    const calledPaths = mockFetchByPath({
      '/api/session': () => new Response(JSON.stringify({ message: '尚未登入' }), { status: 401 }),
      '/api/users': () => new Response(JSON.stringify({ message: '此帳號已被使用' }), { status: 409 }),
    });

    render(
      <MemoryRouter initialEntries={['/signup']}>
        <App />
      </MemoryRouter>,
    );
    await waitFor(() => expect(screen.getByRole('heading', { name: '建立帳號' })).toBeInTheDocument());

    fireEvent.change(screen.getByLabelText('帳號 ID'), { target: { value: 'user1' } });
    fireEvent.click(screen.getByRole('button', { name: '確認建立帳號' }));

    await waitFor(() => expect(screen.getByRole('alert')).toHaveTextContent('此帳號已被使用'));
    expect(screen.getByLabelText('帳號 ID')).toHaveValue('user1');
    // 帳號是否重複只有後端知道：畫面要真的呼叫 uc-create-user，再依 fail p2 呈現（CR-008）
    expect(calledPaths.some((path) => path.endsWith('/api/users'))).toBe(true);
  });

  it('密碼留白時確認建立帳號成功，觸發 uc-create-user，並導向 s-login', async () => {
    mockFetchByPath({
      '/api/session': () => new Response(JSON.stringify({ message: '尚未登入' }), { status: 401 }),
      '/api/users': () => new Response(JSON.stringify({ username: 'user2', displayName: 'user2' }), { status: 201 }),
    });

    render(
      <MemoryRouter initialEntries={['/signup']}>
        <App />
      </MemoryRouter>,
    );
    await waitFor(() => expect(screen.getByRole('heading', { name: '建立帳號' })).toBeInTheDocument());

    fireEvent.change(screen.getByLabelText('帳號 ID'), { target: { value: 'user2' } });
    fireEvent.click(screen.getByRole('button', { name: '確認建立帳號' }));

    await waitFor(() => expect(screen.getByRole('heading', { name: '登入' })).toBeInTheDocument());
  });

  it('已經有帳號連結導向 s-login', async () => {
    await renderSignup();

    fireEvent.click(screen.getByRole('link', { name: '已經有帳號？前往登入' }));

    await waitFor(() => expect(screen.getByRole('heading', { name: '登入' })).toBeInTheDocument());
  });
});
