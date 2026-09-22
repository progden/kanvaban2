# kanban-frontend

Kanban 看板的前端（React 19 + TypeScript + Vite），對應 `.dev/` 下的規格與 UI 短規格實作。開發時經由 Vite 的 `/api` proxy 轉發到 `kanban-spring`（預設 `localhost:8080`），讓 HttpSession cookie 能同源帶上。

## 開發指令

```bash
pnpm install       # 安裝依賴（這個 repo 用 pnpm，不是 npm）
pnpm dev           # 啟動開發伺服器（localhost:5173）
pnpm build         # 型別檢查 + build
pnpm lint          # oxlint
pnpm test          # 單元／元件測試（vitest）
pnpm e2e           # 端對端測試（playwright）
pnpm e2e:ui        # 端對端測試，開 Playwright UI 模式
```

## 跑 e2e 前要先準備好

`pnpm e2e` 只會幫你啟動/重用前端 dev server（見 `playwright.config.ts` 的 `webServer`），下面這些要自己先啟動：

- PostgreSQL（`kanban` 資料庫，帳密見 `kanban-spring/src/main/resources/application.yml` 的預設值）
- 後端：`../gradlew :kanban-spring:bootRun`（`localhost:8080`）

## 目錄結構

- `src/api/`：對應後端各 Controller 的請求／回應型別與呼叫函式
- `src/auth/`、`src/board/`、`src/canvas/`、`src/pages/`、`src/widgets/`：依畫面／功能模組分的元件
- `src/canvas/itemComponentRegistry.tsx`：畫布上「加入元件」的元件登記表，新增一種可放上畫布的元件就在這裡 `registerItemComponent`
- `e2e/`：Playwright 測試，依主體分類（跟 `src/` 的模組對應），例如 `e2e/board/`、`e2e/canvas/`；`e2e/support/` 放共用的 API 資料準備（`api.ts`）與 fixture（`fixtures.ts`）
