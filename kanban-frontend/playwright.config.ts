import { defineConfig, devices } from '@playwright/test';

// e2e 測試依「用元件主體分類」放在 e2e/<主體>/，對照 src 目錄結構
// （auth／board／canvas（含 canvas/members）／widgets）。後端（kanban-spring，8080）與
// PostgreSQL 需另外啟動，這裡只負責啟動／重用前端 dev server；baseURL 走前端的 /api proxy
// （見 vite.config.ts），讓 HttpSession cookie 與畫面同源。
export default defineConfig({
  testDir: './e2e',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  reporter: 'html',
  use: {
    baseURL: 'http://localhost:5173',
    trace: 'on-first-retry',
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:5173',
    reuseExistingServer: true,
    timeout: 30_000,
  },
});
