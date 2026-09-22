// s-signup／s-login（uc-create-user／uc-login／uc-logout）＋ ProtectedRoute／GuestOnlyRoute 的
// happy path：註冊 → 登入 → 進入 /boards → 登出 → 回到 /login；順便驗證未登入不能看 /boards、
// 已登入不能看 /login（各自導向對方）。
import { expect, test } from '@playwright/test';
import { makeCredentials } from '../support/api';

test.describe('auth', () => {
  test('signup → login → boards → logout 走完整個 happy path', async ({ page }) => {
    const creds = makeCredentials('flow');

    await page.goto('/signup');
    await page.getByLabel('帳號 ID').fill(creds.username);
    await page.getByLabel('顯示名字').fill(creds.displayName);
    await page.getByLabel('密碼').fill(creds.password);
    await page.getByRole('button', { name: '確認建立帳號' }).click();

    await expect(page).toHaveURL(/\/login$/);

    await page.getByLabel('帳號 ID').fill(creds.username);
    await page.getByLabel('密碼').fill(creds.password);
    await page.getByRole('button', { name: '登入' }).click();

    await expect(page).toHaveURL(/\/boards$/);
    await expect(page.getByText(creds.displayName)).toBeVisible();

    // 已登入時 /login、/signup 應被 GuestOnlyRoute 導回 /boards。
    await page.goto('/login');
    await expect(page).toHaveURL(/\/boards$/);

    await page.getByRole('button', { name: '登出' }).click();
    await expect(page).toHaveURL(/\/login$/);

    // 登出後 ProtectedRoute 應擋掉 /boards，導回 /login。
    await page.goto('/boards');
    await expect(page).toHaveURL(/\/login$/);
  });
});
