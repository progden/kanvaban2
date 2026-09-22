// 「看板成員」canvas item ＋ s-member-management（uc-invite-member）：放上「看板成員」item →
// 點開成員管理 → 邀請成員 → 成員清單與 item 上的頭像列都要看得到新成員。
import { test, expect, api } from '../../support/fixtures';

test.describe('member management', () => {
  test('邀請成員後出現在成員清單與看板成員 item', async ({ page, ownerBoard, member }) => {
    await api.placeItem(page.context().request, ownerBoard.id, 'board-members', 1000, 40, 260, 160);
    await page.goto(`/boards/${ownerBoard.id}`);

    const trigger = page.getByRole('button', { name: '看板成員，選擇加入成員' });
    await expect(trigger).toBeVisible();
    await expect(trigger.locator('.avatar')).toHaveCount(1); // 一開始只有 Owner 自己

    await trigger.click();
    await expect(page.getByRole('heading', { name: '看板成員' })).toBeVisible();

    await page.getByLabel('邀請成員').fill(member.username);
    await page.getByLabel('邀請角色').selectOption('MEMBER');
    await page.getByRole('button', { name: '邀請' }).click();

    const row = page.locator('.member-management-dialog__row', { hasText: member.displayName });
    await expect(row).toBeVisible();
    await expect(row).toContainText('Member');

    await page.getByRole('button', { name: '關閉' }).click();
    await expect(trigger.locator('.avatar')).toHaveCount(2);
  });
});
