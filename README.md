# Kanban

一個看板應用：Board（Swimlane × Stage）＋ 一個可自由擺放元件的畫布（Canvas），畫布上可以放看板本體、成員清單、工作量／Cycle-Lead-Time／WIP／Throughput-CFD／截止日期提醒等儀表板、Feature/CR 追蹤表、活動紀錄。

![畫布截圖](docs/screenshot-canvas.png)

## 技術棧

- 後端：Java 25 / Spring Boot 4.1.1 / Lombok / Spring Data JPA / PostgreSQL
  - `kanban-core`：純領域模型（`io.progden.kanban.core.domain`），不依賴 Spring／JPA
  - `kanban-spring`：application／web／persistence 層，以及跨 aggregate 的讀取投影（`io.progden.kanban.query.*`）
- 前端：`kanban-frontend`（React 19 / TypeScript / Vite）

## 快速開始

```bash
# 1. 啟動 PostgreSQL（資料庫 kanban，帳密見 kanban-spring/src/main/resources/application.yml）

# 2. 啟動後端（localhost:8080）
./gradlew :kanban-spring:bootRun

# 3. 啟動前端（localhost:5173，經 Vite proxy 轉發 /api 到後端）
cd kanban-frontend
pnpm install
pnpm dev
```

前端更完整的開發指令（build／test／e2e）見 [`kanban-frontend/README.md`](kanban-frontend/README.md)。

## 專案結構

- `.dev/`：規格與設計文件（BDD spec、UI 短規格、CR 紀錄），是行為的唯一依據；細節見 [`CLAUDE.md`](CLAUDE.md)
- `kanban-core`、`kanban-spring`：後端兩個模組
- `kanban-frontend`：前端
- `scripts/`：規格檢查腳本（`spec-check`／`ui-check`／`cr-check`），見 [`scripts/README.md`](scripts/README.md)

## 功能模組（F01～F07）

| 編號 | 模組 | 重點 |
|---|---|---|
| F01 | basic-kanban | Board（含 Swimlane、Stage）與 Card |
| F02 | user-membership | 使用者、BoardMembership、卡片負責人多選、活動紀錄 |
| F03 | kanban-widgets | Cycle/Lead Time、WIP、Throughput/CFD、截止日期提醒 |
| F04 | board-clock | 每個 Board 自己的時鐘 |
| F05 | workload | 依負責人統計工作量 |
| F06 | feature-cr-board | 用看板追蹤 Feature／CR 卡的開發狀態 |
| F07 | canvas-layout | 每個 Board 一個可自由擺放元件的畫布 |
