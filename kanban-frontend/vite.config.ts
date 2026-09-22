/// <reference types="vitest/config" />
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // 後端 kanban-spring 用 HttpSession 保存登入態，前端開發伺服器需經由同源 proxy 轉發 /api，
    // 瀏覽器才會把 session cookie 一併帶上；正式環境預期同源部署，不需要這個設定。
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
  test: {
    environment: 'jsdom',
    setupFiles: ['./src/setupTests.ts'],
    // e2e/ 是 Playwright 的 spec（playwright.config.ts 才認得 test.describe 的簽名），
    // 不排除的話 vitest 預設的 test glob 也會抓到它們，當成 vitest 測試檔硬跑會直接炸掉。
    exclude: ['e2e/**', 'node_modules/**'],
  },
})
