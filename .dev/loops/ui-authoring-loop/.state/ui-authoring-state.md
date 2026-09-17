# UI 撰寫狀態快照（每輪覆寫，20 行內）

- 更新：2026-09-17（執行輪）
- 目前階段：F01～F05 全部收尾完成；F06 剛建立檔頭與 1 個畫面骨架（未討論）；F07 尚未開始
- 上一輪驗證：PASS（範圍 4c43e1d..e62246e，任務 T5.03）
- 本輪任務：T6.01——建立 `.dev/F06-feature-cr-board/ui-feature-cr-board.md`：檔頭 ＋ 1 個畫面標題骨架 `s-feature-cr-board`（所屬 Feature：Feature／CR 追蹤表，類型：儀表板，狀態：未討論），八段內容留空，同 T1.01 模式；任務清單將 T6.01 標為 done
- 已完成的模組：F01 全部 8 個畫面已收尾；F02 全部 9 個畫面已收尾；F03 全部 4 個畫面已收尾（討論中）；F04 全部 1 個畫面已收尾（討論中）；F05 全部 1 個畫面已收尾（討論中）；F06 僅骨架（未討論）；F07 尚未開始
- 已定義的共用 ID：無新增
- 最近 OQ：OQ-41、OQ-42（皆屬 F05，本輪未新增 OQ）
- 下一個任務：T6.02（[F06] 定案 `s-feature-cr-board`（類型：儀表板／列表）：對應 `uc-view-feature-cr-board`，同 T1.02 模式）
- 待注意：OQ 檔任何時候都只能追加，PDCA 也只能追加，發現舊紀錄有缺漏一律用本輪補記說明，不回頭編輯舊 Iteration。F06 spec 名詞定義三張表（實體／欄位／關係）皆為空，資料完全來自「其他名詞」表（Feature 卡／CR 卡／affects 標籤／狀態／orphan CR）與 `uc-view-feature-cr-board` 的 `post`，T6.02 撰寫「資料」段時來源欄需引用這些非 ID 名詞或 `card`／`board` 既有欄位，注意不要發明新 Attribute ID。
