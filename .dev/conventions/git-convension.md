# Commit Message 規範

## 格式

```
[類型](scope) 摘要 (#票號)

body（選填）
```

- **摘要**：祈使句、50 字內、中文、句尾不加標點、不用 emoji
- **scope**：模組名，從既定清單選；跨模組省略
- **票號**：`#123`，放摘要結尾
- **不相容變更**：摘要前加 `!`，body 內寫 `BREAKING CHANGE:` 說明影響

## 類型

| 類型 | 用途 |
|---|---|
| `[spec/design]` | 需求、規格、架構決策、介面定義；不含程式碼 |
| `[dev]` | 功能實作、重構、修 bug |
| `[test]` | 新增或修改測試；不動產品程式碼 |
| `[docs]` | README、註解、操作文件；說明已存在的東西 |
| `[chore]` | 建置、依賴、CI、格式化 |
| `[revert]` | 回退，摘要註明原 commit hash |

## 規則

1. 一個 commit 一件事、一種類型。
2. test 與 dev 同進時，以主要意圖標記；能拆就拆。
3. body 只寫 diff 看不出的事：動機、方案取捨、副作用或風險。不重述做了什麼。
4. pre-commit hook 以正則檢查：

   ```
   ^!?\[(spec/design|dev|test|docs|chore|revert)\](\([a-z-]+\))? .+
   ```

## 範例

```
[spec/design](approval) 新增簽核流程狀態機設計
[dev](approval) 實作簽核退回至前一關卡 (#45)
[test](approval) 補上退回的邊界案例 (#45)
![dev](api) 移除 v1 簽核端點

BREAKING CHANGE: /api/v1/approvals 下線，改用 v2
[revert] 回退 a1b2c3d 的簽核退回實作
``` 