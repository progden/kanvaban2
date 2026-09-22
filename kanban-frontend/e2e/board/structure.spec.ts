// s-swimlane-list／s-stage-list（uc-add-swimlane／uc-add-stage／uc-set-stage-role）：在看板本體 item
// （uc-init-canvas 自動放置，見 support/fixtures.ts）上新增一條 Swimlane、一個 Stage 並設角色。
import { test, expect } from '../support/fixtures';

test.describe('board structure', () => {
  test.beforeEach(async ({ page, ownerBoard }) => {
    await page.goto(`/boards/${ownerBoard.id}`);
    await expect(page.locator('.canvas-item__header', { hasText: 'board' })).toBeVisible();
  });

  test('新增 Swimlane 後出現在看板格線上', async ({ page }) => {
    const swimlaneName = `加班泳道 ${Date.now()}`;

    await page.getByRole('button', { name: '管理 Swimlane' }).click();
    await expect(page.getByRole('heading', { name: '管理泳道' })).toBeVisible();
    await page.getByLabel('新增泳道').fill(swimlaneName);
    await page.locator('.board-structure-panel').getByRole('button', { name: '新增' }).click();
    await expect(page.getByText(swimlaneName).first()).toBeVisible();

    await page.getByRole('button', { name: '關閉面板' }).click();
    await expect(page.locator('.board-grid__swimlane-label', { hasText: swimlaneName })).toBeVisible();
  });

  test('新增 Stage 並設為 START 角色', async ({ page }) => {
    const stageName = `審核 ${Date.now()}`;

    await page.getByRole('button', { name: '管理 Stage' }).click();
    await expect(page.getByRole('heading', { name: '管理階段' })).toBeVisible();
    await page.getByLabel('新增階段').fill(stageName);
    await page.locator('.board-structure-panel').getByRole('button', { name: '新增' }).click();

    const row = page.locator('.board-structure-panel__list li', { hasText: stageName });
    await expect(row).toBeVisible();
    await row.locator('select').selectOption('START');
    await expect(row.locator('select')).toHaveValue('START');

    await page.getByRole('button', { name: '關閉面板' }).click();
    await expect(
      page.locator('.board-grid__stage-header', { hasText: stageName }).locator('.board-grid__stage-role'),
    ).toHaveText('START');
  });
});
