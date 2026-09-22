// s-card-add-dialog／s-card-detail／s-card-assignee-picker（uc-add-card／uc-edit-card／
// uc-add-comment／uc-set-card-assignees／uc-move-card-stage）：新增卡片 → 編輯內容＋留言 →
// 指派負責人 → 移動到 Done（跨 Stage 移動本身走 API，理由見下方註解）。
//
// 預設看板（見 support/fixtures.ts ownerBoard）建立時已經有 1 條泳道「預設泳道」與 3 個 Stage
// 「待辦／進行中／完成」，畫面上沒有 swimlane／stage id 可以掛 data-attribute，這裡用固定的欄位順序
// （待辦＝第 0 欄、完成＝第 2 欄）定位「＋ 新增卡片」按鈕與格子，只在單一泳道情境下成立。
import { test, expect, api } from '../support/fixtures';

test.describe('card', () => {
  test('新增卡片、編輯內容、留言、指派負責人、拖到完成', async ({ page, ownerBoard, member }) => {
    await api.inviteMember(page.context().request, ownerBoard.id, member.username);

    const title = `E2E 卡片 ${Date.now()}`;
    await page.goto(`/boards/${ownerBoard.id}`);
    await expect(page.locator('.canvas-item__header', { hasText: 'board' })).toBeVisible();

    await page.locator('.board-grid__add-card').nth(0).click();
    await expect(page.getByRole('heading', { name: '新增卡片' })).toBeVisible();
    await page.getByLabel('卡片標題').fill(title);
    await page.locator('.dialog-panel').getByRole('button', { name: '新增卡片' }).click();

    const cardEl = page.locator('.board-card', { hasText: title });
    await expect(cardEl).toBeVisible();

    // 編輯內容＋留言
    await cardEl.click();
    await expect(page.locator('.board-card-detail__title')).toHaveText(title);
    await page.getByLabel('描述').fill('E2E 測試補上的描述');
    await page.getByLabel('截止日期').fill('2026-12-31');
    await page.getByLabel('標籤（用、分隔）').fill('e2e、happy-path');
    await page.getByRole('button', { name: '儲存變更' }).click();

    await page.getByLabel('新增留言').fill('這是 e2e 留下的留言');
    await page.getByRole('button', { name: '送出' }).click();
    await expect(page.locator('.board-card-detail__comments')).toContainText('這是 e2e 留下的留言');

    // 指派負責人：候選名單來自 uc-list-card-assignee-candidates，member 已被邀請進來才會出現。
    await page.locator('.board-card-detail__assignees').getByRole('button', { name: '變更' }).click();
    await expect(page.getByRole('heading', { name: '指派負責人' })).toBeVisible();
    await page.getByText(member.displayName).click();
    // 指派負責人對話框疊在卡片詳情對話框上面，兩個 .dialog-panel 同時存在，取最後一個（最上層）。
    await page.locator('.dialog-panel').last().getByRole('button', { name: '儲存' }).click();
    await expect(page.locator('.board-card-detail__assignees .avatar')).toHaveCount(1);

    await page.getByRole('button', { name: '關閉卡片詳情' }).click();
    await expect(cardEl.locator('.board-card__avatars .avatar')).toHaveCount(1);

    // 跨 Stage 移動（uc-move-card-stage）在畫面上是原生 HTML5 拖放（.board-card 的 draggable
    // ＋ onDragStart／onDrop，見 BoardItemContent.tsx）；Playwright／CDP 在無頭 Chromium 下
    // 無法可靠合成真正的原生拖放手勢（dragTo() 與手動 mouse.down/move/up 都測過，dragstart 不會
    // 觸發，這是 Playwright 已知限制，不是這裡的應用程式邏輯問題），所以這一步改用後端 API 驗證
    // 「移到完成」這個 usecase 本身，UI 呈現的部分則由前面新增／編輯／指派卡片已經涵蓋。
    const doneStage = ownerBoard.stages.find((s) => s.name === '完成');
    if (doneStage === undefined) throw new Error('fixture board 缺少「完成」stage');
    const [createdCard] = (await api.listCardsForBoard(page.context().request, ownerBoard.id)).filter(
      (c) => c.title === title,
    );
    if (createdCard === undefined) throw new Error('找不到剛新增的卡片');
    await api.moveCardStage(page.context().request, createdCard.id, doneStage.id);

    await page.reload();
    const doneCell = page.locator('.board-grid__cell').nth(2);
    await expect(doneCell.locator('.board-card', { hasText: title })).toBeVisible();
  });
});
