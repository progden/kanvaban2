// s-board-clock-control（uc-adjust-board-clock／uc-pause-resume-board-clock，CR-014）：從畫布上方
// 「看板時間」按鈕開啟對話框，Owner 調整目標時間、暫停／恢復。
import { test, expect } from '../support/fixtures';

test.describe('board clock', () => {
  test('調整看板時間、暫停後再恢復', async ({ page, ownerBoard }) => {
    await page.goto(`/boards/${ownerBoard.id}`);

    await page.getByRole('button', { name: '看板時間' }).click();
    await expect(page.getByRole('heading', { name: '看板時間' })).toBeVisible();
    await expect(page.getByTestId('clock-control-status')).toHaveText('REALTIME');

    await page.getByLabel('調整目標時間').fill('2026-10-01T09:00');
    await page.getByRole('button', { name: '調整看板時間' }).click();
    await expect(page.getByTestId('clock-control-time')).toContainText('2026-10-01');

    await page.getByRole('button', { name: '暫停' }).click();
    await expect(page.getByTestId('clock-control-status')).toHaveText('PAUSED');

    await page.getByRole('button', { name: '恢復' }).click();
    await expect(page.getByTestId('clock-control-status')).toHaveText('REALTIME');

    await page.locator('.clock-control').getByRole('button', { name: '關閉' }).click();
    await expect(page.getByRole('heading', { name: '看板時間' })).toHaveCount(0);
  });
});
