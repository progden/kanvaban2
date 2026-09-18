// 對應 kanban-spring UserController（POST /api/users、/api/login、/api/logout、GET /api/session）
// 的請求／回應形狀，欄位名稱與後端 record 保持一致。

import { apiClient } from './http';

export interface UserResponse {
  username: string;
  displayName: string;
}

export interface SessionResponse {
  username: string;
}

export function fetchSession(): Promise<SessionResponse> {
  return apiClient.get<SessionResponse>('/api/session');
}

export function login(username: string, password: string): Promise<UserResponse> {
  return apiClient.post<UserResponse>('/api/login', { username, password });
}

export function logout(): Promise<void> {
  return apiClient.post<void>('/api/logout');
}

export function createUser(username: string, displayName: string, password: string): Promise<UserResponse> {
  return apiClient.post<UserResponse>('/api/users', { username, displayName, password });
}
