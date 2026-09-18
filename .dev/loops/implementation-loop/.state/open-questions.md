# open-questions

> 格式沿用 [`與專家協作的提問規則-本體論分析.md`](../../lesson-learned/與專家協作的提問規則-本體論分析.md)：情況欄只能是「引用原文」「兩處矛盾並列」「推論＋所本原文」「覆蓋」四選一並標明種類，逐字引用一律用『』包住，不用「應該」「建議這樣比較好」等說服性字眼代替引用；矛盾類固定給「保留A／保留B／缺區分條件」三個選項；每列開頭固定加 `[Level: <模組>/<任務 ID>]` 標記；新增前先查重，推翻先前結論須明講「推翻 OQ-IMPL-xx」。
>
> 注意：這裡的「情況」四分類跟 `iteration-prompt.md` 第 5 節「自主決策分級」（低風險／高風險／覆蓋來源／環境限制）是兩套不同的分類——分級表講的是「這個判斷有多危險」，這裡的四選一講的是「這句斷言的來源是什麼」。任務清單備註寫「環境限制」是分級表的用語，不可以照搬填進本檔的「情況」欄。

## OQ-IMPL-01

[Level: F03-kanban-widgets/T-06-be-kanban-widgets]
情況：【推論＋所本原文】
spec原文：`.dev/F03-kanban-widgets/spec-kanban-widgets.md`「名詞定義」§實體／§欄位／§關係三張表，逐字內容為『| ID | 名詞 | 所屬 Aggregate | 說明 |』`\n`『|---|---|---|---|』，表頭下沒有任何資料列（欄位、關係兩張表同樣只有表頭）。
推論：由上述原文可知該模組尚未定義任何 `entity`／欄位／`r-` ID，T-06 找不到可引用的依據，無法排出精確的 domain／query 實作範圍。
問題：T-06 是否可以先依「其他名詞」段（Lead Time／Cycle Time／WIP／Aging／Throughput／CFD／asOf 的文字說明）推導範圍動工，還是必須等三張表補齊？
選項：A. 等待 `spec-migration-loop` 完成 F03 遷移（三張表補齊）後再把 T-06 解除 `blocked`；B. 人工先手動補三張表；C. 以上皆非。
事實：影響任務 T-06、T-15（前端對應畫面）共 2 個任務。
狀態：待解除。

## OQ-IMPL-02

[Level: F05-workload/T-07-be-workload]
情況：【推論＋所本原文】
spec原文：`.dev/F05-workload/spec-workload.md`「名詞定義」§實體／§欄位／§關係三張表，逐字內容同 OQ-IMPL-01（表頭下無資料列）。
推論：同 OQ-IMPL-01，T-07 找不到 `card`／`user` 以外的模組專屬實體定義可引用（Active Card／Workload／未指派僅有「其他名詞」文字說明）。
問題：同 OQ-IMPL-01。
選項：同 OQ-IMPL-01（A／B／C）。
事實：影響任務 T-07、T-16（前端對應畫面）共 2 個任務。
狀態：待解除。

## OQ-IMPL-03

[Level: F06-feature-cr-board/T-08-be-feature-cr-board]
情況：【推論＋所本原文】
spec原文：`.dev/F06-feature-cr-board/spec-feature-cr-board.md`「名詞定義」§實體／§欄位／§關係三張表，逐字內容同 OQ-IMPL-01（表頭下無資料列）。
推論：同 OQ-IMPL-01，T-08 找不到 Feature 卡／CR 卡／affects 標籤以外的正式欄位定義（目前只有「其他名詞」文字說明其標籤格式）。
問題：同 OQ-IMPL-01。
選項：同 OQ-IMPL-01（A／B／C）。
事實：影響任務 T-08、T-17（前端對應畫面）共 2 個任務。
狀態：待解除。

## OQ-IMPL-04

[Level: F03-kanban-widgets/T-15-fe-widgets]
情況：【引用原文】
spec原文：`CLAUDE.md`「文件結構」段：『`ui-<模組>.md`（選填，目前僅 F01 有）：UI 短規格，依 `ui-convention.md`，`ui-check` 解析它』；前端設計稿（`planning-prompt.md` 附的畫面清單，35 個 `project/*.dc.html`）裡也沒有任何檔名對應 F03。
問題：T-15 沒有 `ui-kanban-widgets.md` 可依循的操作表／驗收條件，也沒有既有畫面可參照版面，是否要先补 `ui-*.md` 才能排出前端任務範圍？
選項：A. 等待補上 `ui-kanban-widgets.md` 與對應設計稿後再解除 `blocked`；B. 由 Dev 自行設計最簡版面先做、標記「待對照設計稿」；C. 以上皆非。
事實：影響任務 T-15，依賴 T-06（見 OQ-IMPL-01，同樣 blocked）。
狀態：待解除。

## OQ-IMPL-05

[Level: F05-workload/T-16-fe-workload]
情況：【引用原文】
spec原文：同 OQ-IMPL-04 的 `CLAUDE.md` 引用；設計稿清單裡沒有檔名對應 F05。
問題：同 OQ-IMPL-04。
選項：同 OQ-IMPL-04（A／B／C）。
事實：影響任務 T-16，依賴 T-07（見 OQ-IMPL-02，同樣 blocked）。
狀態：待解除。

## OQ-IMPL-06

[Level: F06-feature-cr-board/T-17-fe-feature-cr-board]
情況：【引用原文】
spec原文：同 OQ-IMPL-04 的 `CLAUDE.md` 引用；設計稿清單裡沒有檔名對應 F06。
問題：同 OQ-IMPL-04。
選項：同 OQ-IMPL-04（A／B／C）。
事實：影響任務 T-17，依賴 T-08（見 OQ-IMPL-03，同樣 blocked）。
狀態：待解除。
