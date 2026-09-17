# UI 撰寫狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-17（執行輪）
- 目前階段：階段 2（F02 user-membership）審查輪新增的 D-14～D-16 收尾中；D-14、D-15 已完成，D-16 待處理，處理完才進入階段 3（F03）
- 上一輪任務：D-15——處理 `uc-member-add-card` 沒有畫面觸發的 DS-06 warning。判斷後採用選項 (b)：`uc-add-card`（roles r-user）與 `uc-member-add-card`（roles r-board-member）在同一個 `s-card-add-dialog`「確認新增」操作列上，roles 互斥（皆為單一角色），`ui-check` DS-05 無法讓兩個 uc 同列同時觸發，判定為關係推不出來；新增 OQ-30【引用原文】，`s-card-add-dialog`「無權限」段與「待確認事項」加 ⚠️，畫面狀態改回「討論中」。D-15 已標 `done`
- 下一個任務：D-16（內容見 `ui-authoring-tasks.md`，尚未讀取細節，下一輪需先讀該任務描述）
- 已完成的模組：F01 全部 8 個畫面（本輪 `s-card-add-dialog` 狀態改回「討論中」，其餘 7 個仍「已定案」）；F02 全部 9 個畫面已收尾；D-16 仍待處理，處理完才算 F02 真正收尾
- 已定義的共用 ID：無新增
- 最近 OQ：OQ-30（`s-card-add-dialog` 的 `uc-add-card`／`uc-member-add-card` 角色互斥問題，見 PDCA Iteration 40）
- 待注意：OQ 表新增列時務必檢查『』引用所在行不能同時含有多個模組的 spec 檔名／短名（`verify-quotes.py` 的 `spec_for_line` 用整行文字比對，第一個命中的模組就決定用哪份 spec 驗證全部引用，混寫會導致跨模組引用誤判失敗，見本輪除錯過程）；反引號只給 entity／`entity.attr`／`r-`／`uc-`／`ev-`／`s-` 六種 ID 用，工具名稱（如 ui-check）與 crud 片段（如 {card: C}）要用「」或不加符號，否則觸發 REF-07；跨模組 `ui-check` 記得帶 `--spec ".dev/F[0-9][0-9]-*/spec-*.md"` 且把相關模組 ui 檔一起傳入。
