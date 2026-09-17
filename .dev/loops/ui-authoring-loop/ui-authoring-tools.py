#!/usr/bin/env python3
"""ui-authoring loop 共用工具：解析任務清單、PDCA、OQ，並包裝 scripts/ui-check。

供 run-ui-authoring-loop.sh、verify-ui-authoring.sh 與執行輪／審查輪自我檢查使用。
所有路徑相對 repo 根目錄。跟 spec-migration-loop 的 spec-migration-tools.py 同結構，
但沒有 gherkin-diff／tag-diff／changelog-check 這類「跟遷移前備份逐字比對」的檢查
（ui 撰寫是全新產出，沒有 legacy 版本可比對；逐字引用的比對交給 verify-quotes.py）。

子命令：
  actionable <ledger>                         列出可執行任務 ID（doing → D todo → T/G todo，依賴皆 done）
  rows <ledger> <id...>                       輸出指定任務的原始表格列
  status-summary <ledger>                     各狀態數量與 blocked 列
  check-ledger <base> <head> <mode> <gates>   任務清單只改狀態欄、只追加 D-xx（mode = exec | review）
  last-task <pdca>                            最後一則 PDCA 標題中的任務編號
  last-act <pdca>                             最後一則 PDCA 的 Act 段落
  check-pdca-append <base> <head>             PDCA 只追加、新標題格式正確、四段齊全、Check 有 ui-check 結果
  check-append <base> <head>                  一般 append-only 檢查（審查紀錄／OQ 檔共用的底層檢查）
  check-oq <base> <head>                      OQ 檔只追加，且每個 OQ-xx 欄數正確、「採用」「依據」非空
  expected-ui-files                           列出六個模組預期的 ui-<模組>.md 路徑（依 .dev/F??-*/ 推導，不要求已存在）
  ui-files                                    列出目前已存在的 ui-<模組>.md
  error-count <ui檔|all>                      該 ui 檔（或全部既有 ui 檔）的 error 數；檔案不存在視為 0
  warn-count <ui檔|all>                       同上，warn 數
  errors-json                                 印出 {ui檔: error 數}（含 all；不存在的預期檔也列 0）
  accept-check <ledger> (--done | <id...>)    評估驗收條件中的機械條件 ui-check(<arg>)=0
  task-files <ledger> <id>                    任務對應的 ui 檔（依任務欄開頭的 [F0x]；不要求已存在）

ui-check 的結果可用環境變數 FINDINGS_CACHE_DIR 指定快取目錄（verify 用，避免同一輪重複執行）。
"""

import glob
import json
import os
import re
import subprocess
import sys

ROW_ID = re.compile(r"^(T\d+\.\d+|G\d+|D-\d+)$")
STATUSES = {"todo", "doing", "done", "blocked"}
HEADING = re.compile(r"^## Iteration (\d+) — .+ — (T\d+\.\d+|G\d+|D-\d+)\s*$")
MODULE_TAG = re.compile(r"^\[(F\d{2})\]")
TOKEN = re.compile(r"ui-check\(([^)]*)\)=0")
OQ_HEADER = ["編號", "日期", "模組", "情況", "選項", "採用", "依據", "狀態"]

# exec 模式下允許的狀態轉換；blocked 只能由人工解除
EXEC_TRANSITIONS = {
    "todo": {"todo", "doing", "done", "blocked"},
    "doing": {"doing", "done", "blocked"},
    "done": {"done"},
    "blocked": {"blocked"},
}


def read(path):
    if not os.path.exists(path):
        return ""
    with open(path, encoding="utf-8") as f:
        return f.read().replace("\r\n", "\n")


# ---------------------------------------------------------------- 任務清單


def parse_ledger(path):
    rows = []
    for line in read(path).split("\n"):
        if not line.startswith("|"):
            continue
        cells = [c.strip() for c in line.strip().strip("|").split("|")]
        if len(cells) != 5 or not ROW_ID.match(cells[0]):
            continue
        deps = [d.strip() for d in cells[4].split(",") if ROW_ID.match(d.strip())]
        rows.append({"id": cells[0], "status": cells[1], "task": cells[2],
                     "accept": cells[3], "deps_raw": cells[4], "deps": deps, "line": line})
    return rows


def actionable(rows):
    status = {r["id"]: r["status"] for r in rows}

    def ready(r):
        return all(status.get(d) == "done" for d in r["deps"])

    doing = [r for r in rows if r["status"] == "doing" and ready(r)]
    fixes = [r for r in rows if r["id"].startswith("D-") and r["status"] == "todo" and ready(r)]
    plans = [r for r in rows if not r["id"].startswith("D-") and r["status"] == "todo" and ready(r)]
    return doing + fixes + plans


def check_ledger(base_path, head_path, mode, gate_dir):
    errors = []
    base = {r["id"]: r for r in parse_ledger(base_path)}
    head_rows = parse_ledger(head_path)
    head = {r["id"]: r for r in head_rows}
    if len(head) != len(head_rows):
        errors.append("任務清單有重複的任務編號")
    for r in head_rows:
        if r["status"] not in STATUSES:
            errors.append(f"{r['id']} 狀態 `{r['status']}` 不合法")
    for rid, old in base.items():
        new = head.get(rid)
        if new is None:
            errors.append(f"{rid} 被刪除（任務只能改狀態，不能刪除）")
            continue
        if (old["task"], old["accept"], old["deps_raw"]) != (new["task"], new["accept"], new["deps_raw"]):
            errors.append(f"{rid} 的任務描述／驗收條件／依賴被修改（只允許修改狀態欄）")
        errors.extend(check_transition(old, new, mode, gate_dir))
    for rid, new in head.items():
        if rid in base:
            continue
        if not rid.startswith("D-"):
            errors.append(f"{rid} 是新增的 T／G 任務（只有人工可以新增，loop 只能新增 D-xx）")
        elif new["status"] != "todo":
            errors.append(f"{rid} 是新增的 D-xx，狀態必須直接是 todo（本 loop 沒有 proposed／rejected 中間狀態）")
        elif mode == "review":
            pass
    return errors


def check_transition(old, new, mode, gate_dir):
    rid, before, after = old["id"], old["status"], new["status"]
    if before == after:
        return []
    if mode == "review":
        return [f"審查輪不可將 {rid} 由 {before} 改為 {after}（審查輪只能新增 D-xx 或追加審查紀錄）"]
    if rid.startswith("G"):
        if after == "done" and os.path.exists(os.path.join(gate_dir, f"{rid}.approved")):
            return []
        return [f"{rid} 是關卡，只能在 {gate_dir}/{rid}.approved 存在後改為 done"]
    if after not in EXEC_TRANSITIONS.get(before, set()):
        return [f"執行輪不可將 {rid} 由 {before} 改為 {after}"]
    return []


def module_dirs():
    return sorted(glob.glob(".dev/F[0-9][0-9]-*"))


def expected_ui_file_for_dir(d):
    """ui-<模組>.md 的 <模組> 跟 spec-<模組>.md 同名（不是目錄名，兩者偶爾不同，
    例如 F01-basic-kanban 目錄下是 spec-kanban-basic.md）。"""
    specs = sorted(glob.glob(os.path.join(d, "spec-*.md")))
    if specs:
        short = os.path.basename(specs[0])[len("spec-"):]
    else:
        short = os.path.basename(d).split("-", 1)[1] + ".md"
    return os.path.join(d, "ui-" + short)


def expected_ui_files():
    return [expected_ui_file_for_dir(d) for d in module_dirs()]


def ui_for_module(mod):
    for d in module_dirs():
        if os.path.basename(d).startswith(mod + "-"):
            return expected_ui_file_for_dir(d)
    return None


def task_files(row):
    m = MODULE_TAG.match(row["task"])
    if not m:
        return []
    path = ui_for_module(m.group(1))
    return [path] if path else []


# ---------------------------------------------------------------- PDCA／append-only


def pdca_sections(text):
    return re.split(r"(?m)^(?=## Iteration )", text)[1:]


def last_task(path):
    for section in reversed(pdca_sections(read(path))):
        m = HEADING.match(section.split("\n")[0])
        if m:
            return m.group(2)
    return ""


def last_act(path):
    sections = pdca_sections(read(path))
    if not sections:
        return ""
    m = re.search(r"(?ms)^### Act\s*\n(.*?)(?=^---|^## |\Z)", sections[-1])
    return m.group(1).strip() if m else ""


def check_append(base_path, head_path, label=""):
    base_text, head_text = read(base_path), read(head_path)
    if not head_text.startswith(base_text):
        return [f"{label}舊內容被修改或刪除（只能在檔尾追加）"], ""
    return [], head_text[len(base_text):]


def check_pdca_append(base_path, head_path):
    errors, added = check_append(base_path, head_path, "PDCA ")
    if errors:
        return errors
    sections = pdca_sections(added if added.startswith("## ") else "\n" + added)
    if not sections:
        return ["本輪沒有追加 PDCA 紀錄"]
    for sec in sections:
        title = sec.split("\n")[0]
        if not HEADING.match(title):
            errors.append(f"PDCA 標題格式錯誤：`{title}`（應為 `## Iteration <n> — <YYYY-MM-DD HH:MM> — <任務編號>`）")
        for part in ("Plan", "Do", "Check", "Act"):
            if not re.search(rf"(?m)^### {part}\s*$", sec):
                errors.append(f"PDCA「{title}」缺少 ### {part}")
        check = re.search(r"(?ms)^### Check\s*\n(.*?)(?=^### |\Z)", sec)
        if check and "error(s)" not in check.group(1) and "找不到任何 ui 檔" not in check.group(1):
            errors.append(f"PDCA「{title}」的 Check 沒有貼 ui-check 的結果行（`N error(s), M warning(s)`）")
    return errors


def table_rows(text, header):
    rows, in_table = [], False
    for line in text.split("\n"):
        s = line.strip()
        if not s.startswith("|"):
            if in_table:
                break
            continue
        cells = [c.strip() for c in s.strip("|").split("|")]
        if not in_table:
            in_table = cells == header
            continue
        if set("".join(cells)) <= set("-: "):
            continue
        rows.append(cells)
    return rows


def check_oq(base_path, head_path):
    errors, _ = check_append(base_path, head_path, "OQ 檔")
    seen = set()
    for cells in table_rows(read(head_path), OQ_HEADER):
        if len(cells) != len(OQ_HEADER):
            errors.append(f"OQ 表格列欄數應為 {len(OQ_HEADER)}：`{' | '.join(cells)}`")
            continue
        oid = cells[0]
        if not re.fullmatch(r"OQ-\d{2,}", oid):
            errors.append(f"OQ 編號格式錯誤：`{oid}`（應為 OQ-01 起流水號）")
        if oid in seen:
            errors.append(f"OQ 編號重複：{oid}")
        seen.add(oid)
        if not cells[5]:
            errors.append(f"{oid} 缺少「採用」欄")
        if not cells[6]:
            errors.append(f"{oid} 缺少「依據」欄")
        if not re.match(r"^\[Level: [^\]]+\]", cells[3]):
            errors.append(f"{oid} 「情況」欄開頭缺少 `[Level: <模組>/<uc 或 Screen ID>]` 標記")
    return errors


# ---------------------------------------------------------------- ui-check 包裝


def findings(target_path=None):
    """對既有的 ui 檔跑一次 ui-check --format json；target_path 給單一檔案時只解析它自己
    （不像 spec-check 需要一起解析找跨模組引用，ui-check 一份檔案就是一個模組）。"""
    existing = [p for p in expected_ui_files() if os.path.isfile(p)]
    if not existing:
        return []
    cache_dir = os.environ.get("FINDINGS_CACHE_DIR")
    key = target_path or "__all__"
    cache = os.path.join(cache_dir, key.replace("/", "_") + ".json") if cache_dir else None
    if cache and os.path.exists(cache):
        return json.loads(read(cache))
    paths = [target_path] if target_path else existing
    r = subprocess.run([sys.executable, "scripts/ui-check", "--format", "json", *paths],
                        capture_output=True, text=True)
    if r.returncode not in (0, 1):
        raise SystemExit(f"ui-check 執行失敗：{r.stderr.strip()[:300]}")
    try:
        data = json.loads(r.stdout)
    except json.JSONDecodeError:
        raise SystemExit(f"ui-check 非 JSON 輸出：{r.stdout[:300]} {r.stderr.strip()[:300]}")
    if cache:
        os.makedirs(cache_dir, exist_ok=True)
        with open(cache, "w", encoding="utf-8") as f:
            json.dump(data, f, ensure_ascii=False)
    return data


def count(level, target):
    if target == "all":
        fs = findings(None)
    else:
        if not os.path.isfile(target):
            return 0
        fs = findings(target)
    return sum(1 for x in fs if x["level"] == level and (target == "all" or x["file"] == target))


def eval_token(arg, cache):
    target = arg.strip()
    n = cache.setdefault(target, count("error", target))
    if n:
        return f"ui-check({target}) 應為 0，實際 {n}"
    return None


def accept_check(ledger, targets):
    rows = parse_ledger(ledger)
    if targets == ["--done"]:
        rows = [r for r in rows if r["status"] == "done"]
    else:
        rows = [r for r in rows if r["id"] in set(targets)]
    cache = {}
    errors = []
    for r in rows:
        for arg in TOKEN.findall(r["accept"]):
            msg = eval_token(arg, cache)
            if msg:
                errors.append(f"{r['id']}：{msg}")
    return errors


# ---------------------------------------------------------------- CLI


def report(errors):
    if errors:
        print("\n".join(errors))
    return 1 if errors else 0


def main(argv):
    if len(argv) < 2:
        print(__doc__)
        return 2
    cmd, args = argv[1], argv[2:]
    if cmd == "actionable":
        print("\n".join(r["id"] for r in actionable(parse_ledger(args[0]))))
    elif cmd == "rows":
        wanted = set(args[1:])
        print("\n".join(r["line"] for r in parse_ledger(args[0]) if r["id"] in wanted))
    elif cmd == "status-summary":
        rows = parse_ledger(args[0])
        print(" ".join(f"{s}={sum(1 for r in rows if r['status'] == s)}" for s in sorted(STATUSES)))
        for r in rows:
            if r["status"] == "blocked":
                print(r["line"])
    elif cmd == "check-ledger":
        return report(check_ledger(*args[:4]))
    elif cmd == "last-task":
        print(last_task(args[0]))
    elif cmd == "last-act":
        print(last_act(args[0]))
    elif cmd == "check-pdca-append":
        return report(check_pdca_append(args[0], args[1]))
    elif cmd == "check-append":
        return report(check_append(args[0], args[1])[0])
    elif cmd == "check-oq":
        return report(check_oq(args[0], args[1]))
    elif cmd == "expected-ui-files":
        print("\n".join(expected_ui_files()))
    elif cmd == "ui-files":
        print("\n".join(p for p in expected_ui_files() if os.path.isfile(p)))
    elif cmd in ("error-count", "warn-count"):
        print(count("error" if cmd == "error-count" else "warn", args[0]))
    elif cmd == "errors-json":
        data = {p: count("error", p) for p in expected_ui_files()}
        data["all"] = count("error", "all")
        print(json.dumps(data, ensure_ascii=False, indent=1))
    elif cmd == "accept-check":
        return report(accept_check(args[0], args[1:]))
    elif cmd == "task-files":
        print("\n".join(f for r in parse_ledger(args[0]) if r["id"] == args[1] for f in task_files(r)))
    else:
        print(f"未知的子命令：{cmd}", file=sys.stderr)
        return 2
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
