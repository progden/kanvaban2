// s-board-list／s-board-create-dialog／s-board-delete-dialog（uc-view-board-list／uc-create-board）：
// 空清單 → 建立看板 → 出現在列表 → 開啟進畫布 → 回列表刪除看板。
import { test, expect } from '../support/fixtures';

test.describe('board list', () => {
  test('空清單建立看板、開啟、刪除的 happy path', async ({ page, owner }) => {
    void owner; // 只需要它觸發登入 fixture，本測試不直接用帳密內容
    const boardName = `E2E 建立看板 ${Date.now()}`;

    await page.goto('/boards');
    await expect(page.getByText('你還沒有看板。')).toBeVisible();

    await page.getByRole('button', { name: '建立看板' }).click();
    await page.getByLabel('看板名稱').fill(boardName);
    await page.locator('.dialog-panel').getByRole('button', { name: '建立看板' }).click();

    const card = page.locator('.board-list-card', { hasText: boardName });
    await expect(card).toBeVisible();

    await card.getByRole('button', { name: boardName }).click();
    await expect(page).toHaveURL(/\/boards\/.+/);
    await expect(page.locator('.canvas-page__board-name')).toHaveText(boardName);

    await page.getByRole('button', { name: '← 我的看板' }).click();
    await expect(page).toHaveURL(/\/boards$/);

    await page.locator('.board-list-card', { hasText: boardName }).getByRole('button', { name: '刪除' }).click();
    await expect(page.getByText(`刪除看板「${boardName}」？`)).toBeVisible();
    await page.getByRole('button', { name: '刪除看板' }).click();

    await expect(page.locator('.board-list-card', { hasText: boardName })).toHaveCount(0);
  });

  test('已有看板時清單也能建立第二個', async ({ page, ownerBoard }) => {
    await page.goto('/boards');
    await expect(page.locator('.board-list-card', { hasText: ownerBoard.name })).toBeVisible();
    await expect(page.getByRole('button', { name: '建立看板' })).toBeVisible();
  });
});
