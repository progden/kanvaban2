# 核心領域模型設計（Board Clock）

本文件記錄 `io.progden.kanban.core.domain` 套件中，F04 新增部分的設計決策，對應 [`spec-board-clock.md`](./spec-board-clock.md)。共用原則（ID 型別、不變條件回報方式）見 [`../F01-basic-kanban/design.md`](../F01-basic-kanban/design.md)。

## 設計決策

### 1. BoardClock 屬於 Board 內部狀態，不是獨立 Aggregate

與提出人確認：Board Clock 只跟需要計算 Cycle Time／Lead Time 的 `Board`、`Card` 兩個 aggregate 有關，`User` 建立、`BoardMembership`（邀請／角色變更）維持系統時間，不受影響。因此 `BoardClock` 不獨立成 aggregate root，而是 `Board` 的內部欄位（同 `Swimlane`／`Stage` 的模式）：

| 欄位 | 型別 | 說明 |
|---|---|---|
| mode | enum | `REALTIME` / `PAUSED` |
| offsetMs | long | REALTIME 時：`now = wallClock + offsetMs` |
| pausedAt | Instant | PAUSED 時：`now = pausedAt`，不隨 wall clock 走 |
| lastEventAt | Instant | 用於單調性檢查 |

### 2. 時間來源改為注入的 Clock，不直接呼叫 `Instant.now()`

`Board`、`Card` 內部不再直接呼叫 `Instant.now()`；每個會寫入事件時間的方法改為接受呼叫端傳入的「當下時間」（即 `Board.boardClock().now()` 的結果），維持 `kanban-core` 純領域模型、不依賴 `java.time.Clock` 以外任何框架的原則。呼叫端（`kanban-spring` 的 application 層／測試中的 step）負責先讀 `Board` 的時鐘狀態算出 `now`，再傳進 `Board`／`Card` 的方法。

### 3. 單調性檢查放在寫入端

任何會產生新事件的方法（`addSwimlane`、`removeStage`、`Card.create` 等）在寫入前檢查傳入的 `now`：若 `now < lastEventAt` 則拋出新的 `DomainException`（例如 `ErrorCode.BOARD_TIME_BEFORE_LAST_EVENT`）。往回調整時鐘本身（單純設定 `offsetMs`／`pausedAt`）不受此限制，只有「寫入新事件」時才檢查——這樣使用者可以把時鐘調回去「回看」，但不能在過去的時間點插入新資料。

### 4. Cycle Time／Lead Time 與 Stage 角色重新指派的關係（CR-003 相關澄清）

F03 的 Cycle Time／Lead Time 是查詢時（`asOf`）依 Stage 目前的角色設定，去解讀已經記錄下來的 `stageVisits`（每次進出 Stage 的起訖時間）。也就是說：

- Stage 的 `role` 只是一個「目前設定值」，不寫進事件、不影響 `stageVisits` 的記錄內容。
- 重新指派 START／DONE 角色是純粹的設定變更，圖表下一次查詢時會用新的角色設定去重新解讀同一份 `stageVisits`，不需要、也不會回頭改寫歷史事件。

這代表 CR-003（Stage 角色）與 CR-004（Board Clock）彼此獨立：角色設定變更不需要時間點資訊，只有「事件本身何時發生」才需要 Board Clock。

### 5. 系統時鐘取樣不遞減（`lastSystemNow`）

`Instant.now()` 在虛擬機／NTP 校時環境（例如本專案開發用的 WSL2）下並非保證嚴格遞增，兩次取樣間偶爾會回退幾百毫秒；`BoardClock` 若直接拿這個抖動值算 `now()`／`adjustTo()`／`resume()`，會誤觸第 3 節的單調性檢查（`BOARD_CLOCK_BEHIND_LAST_EVENT`）。`BoardClock` 因此新增一個私有欄位 `lastSystemNow`，並在 `now()`／`adjustTo()`／`resume()` 讀取 `systemNow` 引數的地方，一律先經過私有方法 `clampSystemNow(Instant systemNow)` 夾住：取樣值早於 `lastSystemNow` 就沿用舊值，否則記下新值。這只影響「系統時鐘取樣」本身，不影響 `adjustTo` 的 `newTime` 引數（使用者仍可明確把看板時間調到過去）與 `recordEvent` 對明確調到過去後續建立新事件仍會擋下的行為。

副作用：系統時鐘往回撥（例如 NTP 把快了的時鐘調慢、手動把系統時間調早）時，即時模式（`REALTIME`）下的看板時間會停在最後一次取樣，直到系統時間追上為止；往前跳會照常反映。這是刻意的取捨——`spec-board-clock.md` 沒有任何 Scenario 涵蓋或依賴「系統時鐘取樣抖動／跳動也該即時反映」這個行為，換取避免抖動誤判單調性檢查。

### 6. REALTIME 模式下呼叫恢復不做事

`Board.resumeClock` 原本直接呼叫 `BoardClock.resume`，該方法對 `pausedAt` 執行 `toEpochMilli()`；新建看板或已恢復的看板 `pausedAt` 為 `null`，重複呼叫會拋出 `NullPointerException`。`spec-board-clock.md` 只定義「看板時間目前為暫停狀態」時的恢復，沒有涵蓋「時鐘不是暫停狀態時呼叫恢復」這個情境，屬於實作缺陷而非規格未決。`Board.resumeClock` 改為時鐘不是暫停狀態時直接返回，不呼叫 `BoardClock.resume`、不新增活動紀錄；`pauseClock` 在已暫停時的行為維持不變（`BoardClock.pause` 本身不會出錯）。

## 尚未定案

- 調整／暫停時鐘是否記錄活動紀錄、是否限定 Owner 專屬操作：見 `spec-board-clock.md` Open Questions。
- PAUSED 模式下事件排序用的 `sequence` 欄位，需先確認現有 event store（`kanban-spring`）實作現況，屬於 CR-004 範圍。

## 實作狀態

`Board`／`BoardClock` 已實作（`adjustClock`／`pauseClock`／`resumeClock`／`newEventTime`）。另有唯讀查詢 `Board.isClockPaused()`（委派 `BoardClock` package-private 的 `isPaused()`），供應用層判斷時鐘模式。
