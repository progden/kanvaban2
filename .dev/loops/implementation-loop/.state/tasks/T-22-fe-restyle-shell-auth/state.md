# T-22-fe-restyle-shell-auth state

> 2026-09-19 Review 第 3 輪 收尾，status＝`done`。每輪由 `loopctl finish` 覆寫。

T-22-fe-restyle-shell-auth Review 第 3 輪：附保留核准（done）
- 自跑 pnpm test（16/16 通過）、pnpm lint（exit=0）、pnpm build（成功），git status 乾淨
- D-01～D-06 都修好了；品牌欄三段結構與 D-06 各項數值，已對照 Login／Signup／BoardList.dc.html 逐項確認
- 任務邊界：只有 kanban-frontend 7 個檔＋本任務的 .state 目錄
- 保留：OQ-T-22-fe-restyle-shell-auth-01（高、不阻塞，接手：人工）：產品名稱占位字與品牌文案／插圖要不要保留
- 極小視覺差異（TopBar 品牌 gap 10px 對設計稿 12px、註冊頁產品名稱 letter-spacing）已記在審查紀錄，不擋合併
