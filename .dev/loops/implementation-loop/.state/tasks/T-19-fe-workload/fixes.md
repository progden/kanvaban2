# T-19-fe-workload 修正任務（D-xx）

> 由 `loopctl fix add`／`fix done` 維護，不要直接編輯。

## 第 1 輪 Review 退回

| ID | 回合 | 狀態 | 延續 | 描述 |
|---|---|---|---|---|
| D-01 | 1 | todo | — | Viewer 角色不可拖曳的決定被當成「低風險技術決定」只寫在 decision-log，沒有依規則書第 5 節開成 OQ。 問題在哪： - `.dev/F05-workload/ui-workload.md`（狀態：已定案）「角色與權限」表只有一列，逐字：『｜ `r-user`（F01，跨模組） ｜ 全部成員的工作量與未指派卡片數量 ｜ 檢視工作量表、拖曳成員頭像到卡片追加負責人、點擊成員工作量查看卡片清單 ｜』；同檔「狀態」段逐字：『- 無權限：不適用（F05 spec 僅使用跨模組 `r-user` 一種角色，無角色差異）』。 - `.dev/F02-user-membership/spec-user-membership.md` 角色定義表逐字：『｜ r-board-viewer ｜ Board 唯讀成員 ｜ 被邀請加入 Board 的唯讀角色，可檢視看板與相關統計圖表，不能新增／編輯／移動／刪除任何內容，也不能碰成員管理、看板結構或刪除 Board ｜』；同檔「待釐清」逐字：『新增 `r-board-viewer`（唯讀角色）後，既有檢視類 use case（例如 `uc-view-card-assignees`、`uc-list-cards-by-assignee`、`uc-view-board-activity-log`，以及 F03／F04／F05／F06 各檢視類 use case）目前 roles 欄位只列 `r-board-owner`／`r-board-member`，尚未逐一檢視是否也要讓 `r-board-viewer` 檢視；本次只新增角色定義本身，範圍不含這項全面盤點，待後續另行處理。』 這是兩份定稿文件對同一件事的說法不一致（ui-workload.md 說「無角色差異」、spec-user-membership.md 說 Viewer 不能編輯），而且上游 spec 自己白紙黑字說 F05 的 Viewer 適用範圍「待後續另行處理」。`WorkloadDashboard.tsx:35` 的 `setCanDrag(self !== undefined && self.role !== 'VIEWER')` 產生了 ui-workload.md 沒寫的使用者可見行為差異，屬規則書第 5 節的「高風險」（spec 沒講清楚該怎麼實作），不是「技術實作細節」。 怎樣才算修好： 1. 用 `loopctl oq add --level 高 --blocking no --owner 人工 --reason-code spec-conflict --scope s-workload-dashboard/uc-drag-assign-card-owner` 開一則 OQ，內文用「兩處矛盾並列」情況，把上面兩段原文逐字列出（不要縮寫），問題寫「`s-workload-dashboard` 的成員頭像是否要對 `r-board-viewer` 停用拖曳」，選項至少含「A. 維持現行實作（Viewer 不可拖曳）」「B. 依 ui-workload.md 不做角色差異」。 2. 在 decision-log 這輪紀錄的「待確認事項」補上這則 OQ 的編號（目前該段只列了 OQ-T-19-fe-workload-01）。 3. 程式碼本身可以維持現狀（照上游 spec 做，引用方向 spec ← ui），不必改。 |
