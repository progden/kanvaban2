// s-canvas（uc-place-item／uc-move-item／uc-resize-item／uc-reorder-item／uc-set-viewport）：
// 加入一個元件 → 選取、拖曳移動、用把手調整大小、置頂 → 縮放檢視區並重新整理後仍保留。
import { test, expect, api } from '../support/fixtures';

// 預設 1280x720 視窗下，拖曳把手把元件放大到夠明顯的差異時，目標座標會超出視窗右緣；瀏覽器對
// 視窗外座標的合成滑鼠事件行為不可靠（拖曳效果會整個消失，不是被夾到邊界），所以這裡放大視窗，
// 讓元件與拖曳終點都留在視窗內。
test.use({ viewport: { width: 1600, height: 1000 } });

test.describe('canvas items', () => {
  test('透過「＋ 加入元件」放置元件後出現在畫布上', async ({ page, ownerBoard }) => {
    await page.goto(`/boards/${ownerBoard.id}`);
    await expect(page.locator('.canvas-item__header', { hasText: 'board' })).toBeVisible();

    await page.getByRole('button', { name: '＋ 加入元件' }).click();
    await expect(page.getByRole('heading', { name: '加入元件' })).toBeVisible();
    await page.getByLabel('元件識別碼').selectOption('s-duedate-reminder');
    await page.getByLabel('X').fill('1000');
    await page.getByLabel('Y').fill('40');
    await page.locator('.dialog-panel').getByRole('button', { name: '加入' }).click();

    await expect(page.locator('.canvas-item__header', { hasText: 's-duedate-reminder' })).toBeVisible();
  });

  test('選取元件後可拖曳移動、用把手調整大小、置頂', async ({ page, ownerBoard }) => {
    const item = await api.placeItem(page.context().request, ownerBoard.id, 's-wip-dashboard', 1000, 40, 300, 200);
    await page.goto(`/boards/${ownerBoard.id}`);

    const itemLocator = page.getByTestId(`canvas-item-${item.id}`);
    await expect(itemLocator).toBeVisible();

    // 拖曳移動：mousedown 在 header 上、移動一段距離、mouseup，確認畫面座標真的變了。
    // mouseup 後 commitMoveDrag 會非同步打 PATCH /move 存檔，這裡等它回來再做下一個拖曳，
    // 避免跟接下來的 resize 拖曳互相搶著寫 React state（見 CanvasStage.tsx commitMoveDrag／
    // commitResizeDrag：兩者都用「整包 item 換掉」的方式回填，晚到的 move 回應會把 resize
    // 剛更新好的 width/height 蓋回舊值）。
    const before = await itemLocator.boundingBox();
    if (before === null) throw new Error('item not visible');
    const header = itemLocator.locator('.canvas-item__header');
    await header.hover();
    await page.mouse.down();
    await page.mouse.move(before.x + 80, before.y + 60, { steps: 8 });
    await Promise.all([page.waitForResponse((res) => res.url().includes('/move') && res.ok()), page.mouse.up()]);
    const afterMove = await itemLocator.boundingBox();
    if (afterMove === null) throw new Error('item not visible after move');
    expect(afterMove.x).not.toBeCloseTo(before.x, 0);

    // 用右下角把手調整大小。
    const handle = page.getByTestId(`resize-handle-${item.id}-se`);
    await expect(handle).toBeVisible();
    const handleBox = await handle.boundingBox();
    if (handleBox === null) throw new Error('handle not visible');
    await page.mouse.move(handleBox.x + handleBox.width / 2, handleBox.y + handleBox.height / 2);
    await page.mouse.down();
    await page.mouse.move(handleBox.x + 100, handleBox.y + 60, { steps: 8 });
    await page.mouse.up();
    const afterResize = await itemLocator.boundingBox();
    if (afterResize === null) throw new Error('item not visible after resize');
    expect(afterResize.width).toBeGreaterThan(afterMove.width);

    // 置頂：工具列只在單選時出現，元件仍是選取狀態。
    await page.getByRole('button', { name: '置頂' }).click();
    await expect(page.getByRole('button', { name: '移除', exact: true })).toBeVisible();
  });

  test('縮放檢視區後重新整理仍維持相同縮放比例', async ({ page, ownerBoard }) => {
    await page.goto(`/boards/${ownerBoard.id}`);
    const zoom = page.getByTestId('viewport-zoom');
    await expect(zoom).toHaveText('100%');

    await Promise.all([
      page.waitForResponse((res) => res.url().includes('/viewport') && res.ok()),
      page.getByRole('button', { name: '放大' }).click(),
    ]);
    await expect(zoom).not.toHaveText('100%');
    const zoomedText = await zoom.textContent();

    await page.reload();
    await expect(page.getByTestId('viewport-zoom')).toHaveText(zoomedText ?? '');
  });
});
