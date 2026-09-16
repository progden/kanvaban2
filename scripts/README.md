# 規格檢查腳本

依 `.dev/conventions/scripts.md` 實作的三支腳本，檢查項目見 `.dev/conventions/checks.md`。

- 需求：Python 3.10+、PyYAML（`pip install pyyaml`）。不需要其他套件。
- 註解與訊息皆為繁體中文；輸出格式 `<檔案>:<行號>: <層級> <檢查 ID>: <訊息>`，有 error 就 exit 1。

## 用法

```bash
# 檢查所有模組規格（預設 .dev/F*/spec-*.md，CR 總表 .dev/CR.md）
./scripts/spec-check
./scripts/spec-check .dev/F01-basic-kanban/spec-kanban-basic.md --format json

# 印矩陣（CRUD、角色 × UseCase、事件表、追溯矩陣），不寫檔
./scripts/spec-check --report
./scripts/spec-check --report --ui '.dev/F*/ui-*.md'   # 追溯矩陣加 Screen 欄
./scripts/spec-check --report --cr CR-024                        # 只印該 CR 相關的列

# 檢查 UI 短規格（自動讀同目錄同模組的 spec）
./scripts/ui-check
./scripts/ui-check --report          # 畫面總表、追溯矩陣、未被畫面觸發的寫入 UseCase

# 比對 CR 影響 ID 與 PR diff（需要 git；--base 必填）
./scripts/cr-check --base origin/main
./scripts/cr-check --base origin/main --cr CR-024   # 明確指定要比對的 CR
```

## 結構

```
scripts/
├── spec-check / ui-check / cr-check   進入點
├── speccheck/
│   ├── model.py          物件模型（scripts.md §1.2）
│   ├── md.py             Markdown 圍欄／標題／表格／反引號解析
│   ├── ids.py            ID 形式判定（spec-convention.md §9）
│   ├── yamlloc.py        帶行號的 YAML 載入
│   ├── parser_spec.py    spec-<模組>.md → 模型
│   ├── parser_design.py  ui-<模組>.md → 模型
│   ├── parser_cr.py      .dev/CR.md → 模型
│   ├── checks/           每個檢查 ID 一個函式，@check 登記
│   ├── report.py         --report 的矩陣
│   └── runner.py         CLI、git diff、base 版模型
└── tests/
    ├── test_checks.py    good fixture 無 error、bad fixture 報出預期 ID、cr-check 端對端、registry ↔ checks.md 對照
    └── fixtures/         依規範寫的範例 spec／ui／CR.md
```

## 測試

```bash
python3 -m unittest discover -s scripts/tests
```

## 掛進 pre-commit

`.git/hooks/pre-commit`（或 pre-commit 框架的 local hook）：

```bash
#!/bin/sh
./scripts/spec-check && ./scripts/ui-check
```

CI 另加 `./scripts/cr-check --base "$PR_BASE_SHA"`。

## 新增一條檢查

1. 在 `.dev/conventions/checks.md` 加一列（ID、層級、規則、訊息）。
2. 在 `scripts/speccheck/checks/<組>.py` 加一個 `@check("XX-nn", "<腳本>", "<層級>")` 函式，只讀模型、回傳 `Finding` 清單。
3. 跑測試：`Registry.test_all_ids_in_checks_md` 會確認兩邊對得上。
