// e2e 測試的資料準備層：直接呼叫後端 API 建立 happy path 前置資料（帳號、Board、Swimlane／Stage、
// Card、成員……），避免每個 spec 都要重跑一次完整 UI 流程。呼叫端一律用 page.context().request，
// 讓這裡發的請求跟畫面共用同一個 BrowserContext 的 cookie jar（HttpSession 才會認得）。
import type { APIRequestContext } from '@playwright/test';

export interface Credentials {
  username: string;
  displayName: string;
  password: string;
}

function uniqueId(prefix: string): string {
  return `${prefix}${Date.now()}${Math.floor(Math.random() * 10_000)}`;
}

export function makeCredentials(prefix: string): Credentials {
  const username = uniqueId(prefix);
  return { username, displayName: `${prefix} 測試使用者`, password: 'e2e-pass-1234' };
}

async function ok(res: { ok(): boolean; status(): number; url(): string; text(): Promise<string> }) {
  if (!res.ok()) {
    throw new Error(`API 呼叫失敗 ${res.status()} ${res.url()}：${await res.text()}`);
  }
}

export async function signup(request: APIRequestContext, creds: Credentials): Promise<void> {
  const res = await request.post('/api/users', { data: creds });
  await ok(res);
}

export async function login(request: APIRequestContext, creds: Pick<Credentials, 'username' | 'password'>): Promise<void> {
  const res = await request.post('/api/login', { data: creds });
  await ok(res);
}

export async function signupAndLogin(request: APIRequestContext, creds: Credentials): Promise<void> {
  await signup(request, creds);
  await login(request, creds);
}

export interface BoardSummary {
  id: string;
  name: string;
  swimlanes: { id: string; name: string; order: number }[];
  stages: { id: string; name: string; order: number; role: string }[];
}

export async function createBoard(request: APIRequestContext, name: string): Promise<BoardSummary> {
  const res = await request.post('/api/boards', { data: { name } });
  await ok(res);
  return res.json();
}

export async function initCanvas(request: APIRequestContext, boardId: string): Promise<void> {
  const res = await request.get(`/api/boards/${boardId}/canvas`);
  await ok(res);
}

export async function addSwimlane(request: APIRequestContext, boardId: string, name: string): Promise<BoardSummary> {
  const res = await request.post(`/api/boards/${boardId}/swimlanes`, { data: { name } });
  await ok(res);
  return res.json();
}

export async function addStage(
  request: APIRequestContext,
  boardId: string,
  name: string,
  beforeStageId: string | null = null,
): Promise<BoardSummary> {
  const res = await request.post(`/api/boards/${boardId}/stages`, { data: { name, beforeStageId } });
  await ok(res);
  return res.json();
}

export async function setStageRole(
  request: APIRequestContext,
  boardId: string,
  stageId: string,
  role: 'NONE' | 'START' | 'DONE',
): Promise<BoardSummary> {
  const res = await request.patch(`/api/boards/${boardId}/stages/${stageId}/role`, { data: { role } });
  await ok(res);
  return res.json();
}

export interface CardSummary {
  id: string;
  title: string;
  swimlaneId: string;
  stageId: string;
  assigneeIds: string[];
}

export async function addCard(
  request: APIRequestContext,
  boardId: string,
  title: string,
  swimlaneId: string,
  stageId: string,
): Promise<CardSummary> {
  const res = await request.post(`/api/boards/${boardId}/cards`, { data: { title, swimlaneId, stageId } });
  await ok(res);
  return res.json();
}

export async function listCardsForBoard(request: APIRequestContext, boardId: string): Promise<CardSummary[]> {
  const res = await request.get(`/api/boards/${boardId}/cards`);
  await ok(res);
  return res.json();
}

export async function moveCardStage(request: APIRequestContext, cardId: string, stageId: string): Promise<CardSummary> {
  const res = await request.post(`/api/cards/${cardId}/move-stage`, { data: { stageId } });
  await ok(res);
  return res.json();
}

export async function inviteMember(
  request: APIRequestContext,
  boardId: string,
  username: string,
  role: 'OWNER' | 'MEMBER' | 'VIEWER' = 'MEMBER',
): Promise<void> {
  const res = await request.post(`/api/boards/${boardId}/members`, { data: { username, role } });
  await ok(res);
}

export interface ItemSummary {
  id: string;
  component: string;
  x: number;
  y: number;
  width: number;
  height: number;
}

export async function placeItem(
  request: APIRequestContext,
  boardId: string,
  component: string,
  x: number,
  y: number,
  width: number,
  height: number,
): Promise<ItemSummary> {
  const res = await request.post(`/api/boards/${boardId}/canvas/items`, {
    data: { component, x, y, width, height, anchor: 'canvas', movable: true, resizable: true, removable: true },
  });
  await ok(res);
  return res.json();
}
