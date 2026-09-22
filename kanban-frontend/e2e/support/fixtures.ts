// 共用 fixture：大部分 spec 只關心「已登入、已有一個 Board、Board 已開過畫布（board item 已存在）」
// 之後的行為，帳號／登入／建立 Board 本身有專屬的 e2e/auth、e2e/board/board-list.spec.ts 覆蓋，
// 這裡直接用 API 準備好前置狀態，讓其他 spec 專心測自己的 UI 操作。
import { test as base, expect } from '@playwright/test';
import * as api from './api';
import type { BoardSummary, Credentials } from './api';

interface BoardFixtures {
  owner: Credentials;
  ownerBoard: BoardSummary;
  member: Credentials;
}

export const test = base.extend<BoardFixtures>({
  owner: async ({ page }, use) => {
    const creds = api.makeCredentials('owner');
    await api.signupAndLogin(page.context().request, creds);
    await use(creds);
  },
  // 依賴 owner：先登入，再建立 Board 並開一次畫布（uc-init-canvas 會自動放上「board」item，
  // 見 CanvasApplicationService.initCanvas），之後 page.goto(`/boards/${ownerBoard.id}`) 就能直接看到看板本體。
  ownerBoard: async ({ page, owner }, use) => {
    void owner;
    const board = await api.createBoard(page.context().request, `E2E 看板 ${Date.now()}`);
    await api.initCanvas(page.context().request, board.id);
    await use(board);
  },
  // 只註冊帳號、不登入（登入會換掉目前 page 的 session cookie，owner 的測試還要繼續用）；
  // 需要它出現在 ownerBoard 成員清單裡的 spec 自己呼叫 api.inviteMember。
  member: async ({ page }, use) => {
    const creds = api.makeCredentials('member');
    await api.signup(page.context().request, creds);
    await use(creds);
  },
});

export { expect, api };
