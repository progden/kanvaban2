import { afterEach, describe, expect, it, vi } from 'vitest';
import { apiClient, ApiError } from './http';

afterEach(() => {
  vi.restoreAllMocks();
});

describe('apiClient', () => {
  it('回傳成功回應的 JSON 內容', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(JSON.stringify({ username: 'alice' }), { status: 200 }),
    );

    const result = await apiClient.get<{ username: string }>('/api/session');

    expect(result).toEqual({ username: 'alice' });
  });

  it('204 回應時回傳 undefined（例如登出成功）', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(new Response(null, { status: 204 }));

    const result = await apiClient.post('/api/logout');

    expect(result).toBeUndefined();
  });

  it('失敗回應時丟出帶狀態碼與訊息的 ApiError', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(JSON.stringify({ message: '帳號或密碼錯誤' }), { status: 401 }),
    );

    await expect(apiClient.post('/api/login', { username: 'a', password: 'b' })).rejects.toSatisfy((error) => {
      expect(error).toBeInstanceOf(ApiError);
      expect((error as ApiError).status).toBe(401);
      expect((error as ApiError).message).toBe('帳號或密碼錯誤');
      return true;
    });
  });
});
