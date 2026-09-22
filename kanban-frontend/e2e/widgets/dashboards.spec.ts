// F03 四個儀表板（Cycle/Lead Time、WIP、Throughput/CFD、截止日期提醒）＋ F05 工作量、F06 Feature/CR
// 追蹤表、F02 活動紀錄，全部都是「放到畫布上的 item」（見 registerKanbanWidgets／
// canvas/itemComponentRegistry.tsx）。happy path：逐一放上畫布，確認各自載入完成、離開「載入中…」狀態。
import { test, expect, api } from '../support/fixtures';

const SMOKE_ITEMS = ['s-cycle-lead-time-dashboard', 's-wip-dashboard', 's-throughput-cfd-dashboard', 's-duedate-reminder'];

test.describe('widgets', () => {
  test('F03 四個儀表板都能放上畫布並離開載入中狀態', async ({ page, ownerBoard }) => {
    for (const [index, component] of SMOKE_ITEMS.entries()) {
      await api.placeItem(page.context().request, ownerBoard.id, component, 1000, 40 + index * 220, 320, 200);
    }
    await page.goto(`/boards/${ownerBoard.id}`);

    for (const component of SMOKE_ITEMS) {
      const body = page.locator('.canvas-item__header', { hasText: component }).locator('..').locator('.canvas-item__body');
      await expect(body).not.toContainText('載入中…');
    }
  });

  test('工作量儀表板顯示已指派負責人的卡片數', async ({ page, ownerBoard, member }) => {
    await api.inviteMember(page.context().request, ownerBoard.id, member.username);
    const swimlaneId = ownerBoard.swimlanes[0].id;
    const stageId = ownerBoard.stages[0].id;
    await api.addCard(page.context().request, ownerBoard.id, 'workload 用的卡片', swimlaneId, stageId);

    await api.placeItem(page.context().request, ownerBoard.id, 's-workload-dashboard', 1000, 40, 300, 200);
    await page.goto(`/boards/${ownerBoard.id}`);

    await expect(page.getByTestId('workload-member-list')).toBeVisible();
    await expect(page.getByTestId('workload-unassigned-count')).toHaveText('1');
  });

  test('Feature/CR 追蹤表與活動紀錄都能放上畫布', async ({ page, ownerBoard }) => {
    await api.placeItem(page.context().request, ownerBoard.id, 's-feature-cr-board', 1000, 40, 320, 220);
    await api.placeItem(page.context().request, ownerBoard.id, 's-activity-log', 1000, 280, 320, 220);
    await page.goto(`/boards/${ownerBoard.id}`);

    await expect(page.getByTestId('feature-cr-board')).toBeVisible();
    const activityLog = page.getByTestId('activity-log-list');
    await expect(activityLog).toBeVisible();
    // Board 建立、Canvas 初始化這幾步本身就會留下活動紀錄（uc-view-board-activity-log）。
    await expect(activityLog.locator('li').first()).toBeVisible();
  });
});
