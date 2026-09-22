## ADR-T-04-be-board-membership-01：跨 Aggregate 讀取投影放 `io.progden.kanban.query`，且需手動加進 `@ComponentScan`

- 狀態：Accepted
- 日期：2026-09-19
- 提出者：Dev（T-04-be-board-membership）

### 背景（Context）

CLAUDE.md 定義「跨 aggregate 的讀取投影」要放 `io.progden.kanban.query.*`，不放 `kanban-spring` 的 `io.progden.kanban.spring.*` 底下（design-kanban-basic.md／design-user-membership.md 也都把 `uc-view-board-activity-log` 這類跨 Aggregate 合併查詢定位成獨立於任何一個 Aggregate 的投影，理由同 `CardLookupPort`）。本任務為了實作 `uc-view-board-activity-log`（合併 `Board` 自己的活動紀錄與 `board-membership` 的活動紀錄）新增了第一個 `io.progden.kanban.query.BoardActivityLogQueryService`，套件路徑是 `io.progden.kanban.query`，跟 `kanban-spring` 的根套件 `io.progden.kanban.spring` 是**同層的兄弟套件**，不是子套件。

`KanbanApplication`（`io.progden.kanban.spring.KanbanApplication`）標了 `@SpringBootApplication`，預設的元件掃描（component scan）只掃自己所在套件與子套件，也就是只掃得到 `io.progden.kanban.spring.**`；`io.progden.kanban.query.**` 完全掃不到。第一次執行 Cucumber／Smoke Test 時整個 Spring context 啟動失敗，錯誤是 `NoSuchBeanDefinitionException: No qualifying bean of type 'io.progden.kanban.query.BoardActivityLogQueryService'`，花了一輪排查才定位到根因。

### 決策（Decision）

在 `KanbanApplication` 明確加上 `@ComponentScan(basePackages = {"io.progden.kanban.spring", "io.progden.kanban.query"})`。之後任何任務在 `io.progden.kanban.query` 底下新增 `@Service`／`@Component` 類別（例如 F03/F05/F06 的唯讀 projection），都不需要再改這一行——`basePackages` 已經涵蓋整個 `io.progden.kanban.query` 子樹，新增的類別會自動被掃到；只有當未來新增**第三個**跟 `io.progden.kanban.spring`／`io.progden.kanban.query` 不同層級的頂層套件時，才需要再加一個 `basePackages` 項目。

### 考慮過的替代方案（Alternatives）

- 把查詢服務搬到 `io.progden.kanban.spring.query` 底下（變成 `kanban-spring` 的子套件，預設掃描就涵蓋）：不採用，因為 CLAUDE.md 明確訂了 `io.progden.kanban.query.*` 這個套件路徑，改路徑等於違反既有架構文件，且會讓 `kanban-core`／`kanban-spring`／查詢層的套件邊界混在一起，不利於之後可能把查詢層抽成獨立 module。
- 每個查詢服務類別各自標 `@Component` 並額外用 `@Import` 手動註冊：不採用，維護成本隨查詢服務數量線性增加，且容易漏掉。

### 後果（Consequences）

- T-06-be-kanban-widgets、T-07-be-workload、T-08-be-feature-cr-board（都是唯讀 projection，本來就規劃放 `io.progden.kanban.query.*`）不需要再處理這個 `@ComponentScan` 問題，直接把 `@Service` 類別放進 `io.progden.kanban.query` 底下即可被掃到。
- 如果之後有任務把查詢服務放到 `io.progden.kanban.query` 以外的新頂層套件（違反既有慣例的話），一樣會遇到同樣的 `NoSuchBeanDefinitionException`，需要同步更新 `KanbanApplication` 的 `basePackages`。
