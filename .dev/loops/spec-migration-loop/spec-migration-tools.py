#!/usr/bin/env python3
"""spec 遷移 loop 共用工具：解析任務清單、PDCA、OQ、規格 Gherkin，並包裝 scripts/spec-check。

供 run-spec-migration-loop.sh、verify-spec-migration.sh 與執行輪自我檢查使用。所有路徑相對 repo 根目錄。

子命令：
  actionable <ledger>                         列出可執行任務 ID（doing → D todo → T/G todo，依賴皆 done）
  rows <ledger> <id...>                       輸出指定任務的原始表格列
  status-summary <ledger>                     各狀態數量與 blocked／proposed 列
  check-ledger <base> <head> <mode> <gates>   任務清單只改狀態欄、只追加 D-xx（mode = exec | review）
  last-task <pdca>                            最後一則 PDCA 標題中的任務編號
  last-act <pdca>                             最後一則 PDCA 的 Act 段落
  check-pdca-append <base> <head>             PDCA 只追加、新標題格式正確、四段齊全、Check 有 spec-check 結果
  check-append <base> <head>                  一般 append-only 檢查（審查紀錄）
  check-oq <base> <head>                      OQ 檔只追加，且每個 OQ-xx 的「採用」欄非空
  spec-files                                  列出所有 spec 檔（.dev/F*/spec-*.md）
  legacy-path <spec>                          該 spec 的遷移前備份路徑（同目錄 legacy-<檔名>）
  gherkin-extract <檔>                        印出正規化後的行為清單
  gherkin-diff <spec> [--against <檔>|--ref <git ref>]
                                              比對行為清單；預設對同目錄備份檔。完全相同回 0；
                                              只差在 F 編號 ±1 回 3（警告）；其餘差異回 1 並印第一處差異
  tag-diff <spec> [--against <檔>]            每個 Scenario 除 @uc-／@fail- 以外的 tag 集合必須與備份相同
  changelog-check <spec> [--against <檔>]     備份的變更紀錄每一列（日期＋摘要文字）仍在新檔中
  error-count <spec|all>                      全部 spec 一起解析後，該檔（或全部）的 error 數
  warn-count <spec|all>                       同上，warn 數
  errors-json                                 印出 {檔案: error 數}（含 all）
  accept-check <ledger> (--done | <id...>)    評估驗收條件中的機械條件（見任務清單規則）
  crcheck <CR 編號>                           以 runtime/baseline 為 base 跑 cr-check --cr
  task-files <ledger> <id>                    任務對應的 spec 檔（依任務欄開頭的 [F0x]）

spec-check 的結果可用環境變數 FINDINGS_JSON 指定快取檔（verify 用，避免重複執行）。
"""

import difflib
import glob
import json
import os
import re
import subprocess
import sys

ROW_ID = re.compile(r"^(T\d+\.\d+|G\d+|D-\d+)$")
STATUSES = {"todo", "doing", "done", "blocked", "proposed", "rejected"}
HEADING = re.compile(r"^## Iteration (\d+) — .+ — (T\d+\.\d+|G\d+|D-\d+)\s*$")
MODULE_TAG = re.compile(r"^\[(F\d{2})\]")
TOKEN = re.compile(r"(errors|crcheck)\(([^)]*)\)=0")
SPEC_GLOB = ".dev/F*/spec-*.md"
BASELINE_FILE = ".dev/loops/spec-migration-loop/runtime/baseline"
OQ_HEADER = ["編號", "日期", "模組", "情況", "選項", "採用", "依據", "狀態"]

# exec 模式下允許的狀態轉換；blocked 只能由人工解除
EXEC_TRANSITIONS = {
    "todo": {"todo", "doing", "done", "blocked"},
    "doing": {"doing", "done", "blocked"},
    "done": {"done"},
    "blocked": {"blocked"},
    "proposed": {"proposed"},
    "rejected": {"rejected"},
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
            errors.append(f"{rid} 是新增的 T／G 任務（只有人工可以新增，agent 只能新增 D-xx）")
        elif mode == "exec" and new["status"] != "proposed":
            errors.append(f"{rid} 由執行輪新增，狀態必須是 proposed")
        elif mode == "review" and new["status"] not in {"proposed", "todo"}:
            errors.append(f"{rid} 由審查輪新增，狀態只能是 proposed 或 todo")
    return errors


def check_transition(old, new, mode, gate_dir):
    rid, before, after = old["id"], old["status"], new["status"]
    if before == after:
        return []
    if mode == "review":
        if rid.startswith("D-") and before == "proposed" and after in {"todo", "rejected"}:
            return []
        return [f"審查輪不可將 {rid} 由 {before} 改為 {after}（請改以新增 D-xx 指出偏差）"]
    if rid.startswith("G"):
        if after == "done" and os.path.exists(os.path.join(gate_dir, f"{rid}.approved")):
            return []
        return [f"{rid} 是關卡，只能在 {gate_dir}/{rid}.approved 存在後改為 done"]
    if after not in EXEC_TRANSITIONS.get(before, set()):
        return [f"執行輪不可將 {rid} 由 {before} 改為 {after}"]
    return []


def spec_for_module(mod):
    hits = sorted(glob.glob(f".dev/{mod}-*/spec-*.md"))
    return hits[0] if len(hits) == 1 else None


def task_files(row):
    m = MODULE_TAG.match(row["task"])
    if not m:
        return []
    path = spec_for_module(m.group(1))
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


def check_append(base_path, head_path, label):
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
        if check and "error(s)" not in check.group(1):
            errors.append(f"PDCA「{title}」的 Check 沒有貼 spec-check 的結果行（`N error(s), M warning(s)`）")
    return errors


def table_rows(text, header):
    """回傳檔內第一個表頭為 header 的表格資料列（cells list）。"""
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
            errors.append(f"{oid} 缺少「採用」欄（高影響假設必須寫出採用的選項）")
        if not cells[6]:
            errors.append(f"{oid} 缺少「依據」欄（必須指出依據哪個 Scenario）")
    return errors


# ---------------------------------------------------------------- Gherkin 行為清單


def gherkin_blocks(text):
    """回傳 [(起始行 index, 結束行 index, 內容行 list)]；只取 ```gherkin 圍欄，其他圍欄內容略過。"""
    blocks, lines = [], text.split("\n")
    fence, lang, start = None, None, 0
    for i, line in enumerate(lines):
        s = line.strip()
        m = re.match(r"^(`{3,})(.*)$", s)
        if fence is None:
            if m:
                fence, lang, start = m.group(1), m.group(2).strip(), i
        elif m and m.group(1) == fence and not m.group(2).strip():
            if lang == "gherkin":
                blocks.append((start, i, lines[start + 1:i]))
            fence = None
    return blocks


def normalize_line(s):
    s = re.sub(r"\s+", " ", s.replace("`", "")).strip()
    if s.startswith("|"):
        cells = [c.strip() for c in s.strip("|").split("|")]
        return "| " + " | ".join(cells) + " |"
    m = re.match(r"^(我想要|以便)\s*(.*)$", s)
    if m:
        return f"{m.group(1)} {m.group(2)}"
    if s.startswith("Feature:"):
        return "Feature: " + s[len("Feature:"):].strip()
    return s


def gherkin_extract(text):
    out = []
    for _, _, body in gherkin_blocks(text):
        for line in body:
            s = line.strip()
            if not s or s.startswith("#") or s.startswith("@") or s.startswith("身為"):
                continue
            out.append(normalize_line(s))
    return out


def feature_scenarios(text):
    """回傳 [(Feature 名, Scenario 名, 序號, tag 集合)]。"""
    out = []
    for _, _, body in gherkin_blocks(text):
        feature, tags, count = "", [], {}
        for line in body:
            s = line.strip()
            if s.startswith("Feature:"):
                feature = normalize_line(s)[len("Feature: "):]
            elif s.startswith("@"):
                tags.extend(s.split())
            elif re.match(r"^Scenario( Outline)?:", s):
                name = normalize_line(s.split(":", 1)[1])
                key = (feature, name)
                count[key] = count.get(key, 0) + 1
                out.append((feature, name, count[key], tags))
                tags = []
            elif s and not s.startswith("#"):
                tags = []
    return out


def git_show(ref, path):
    r = subprocess.run(["git", "show", f"{ref}:{path}"], capture_output=True, text=True)
    return r.stdout if r.returncode == 0 else ""


def legacy_path(spec):
    return os.path.join(os.path.dirname(spec), "legacy-" + os.path.basename(spec))


def old_text(spec, opts):
    if "--ref" in opts:
        return git_show(opts[opts.index("--ref") + 1], spec)
    path = opts[opts.index("--against") + 1] if "--against" in opts else legacy_path(spec)
    if not os.path.exists(path):
        raise SystemExit(f"找不到遷移前備份：{path}")
    return read(path)


F_NUM = re.compile(r"F(\d{2})")


def only_fnum_shift(a, b):
    """兩行只差在 F 編號，且每一處都剛好差 1。"""
    if F_NUM.sub("F??", a) != F_NUM.sub("F??", b):
        return False
    na, nb = F_NUM.findall(a), F_NUM.findall(b)
    diffs = [(int(x), int(y)) for x, y in zip(na, nb) if x != y]
    return bool(diffs) and all(abs(x - y) == 1 for x, y in diffs)


def gherkin_diff(spec, opts):
    old, new = gherkin_extract(old_text(spec, opts)), gherkin_extract(read(spec))
    if old == new:
        print(f"{spec}：Gherkin 行為與遷移前一致（{len(new)} 行）")
        return 0
    sm = difflib.SequenceMatcher(a=old, b=new, autojunk=False)
    shifted, other = [], []
    for tag, i1, i2, j1, j2 in sm.get_opcodes():
        if tag == "equal":
            continue
        pairs = list(zip(old[i1:i2], new[j1:j2]))
        if tag == "replace" and i2 - i1 == j2 - j1 and all(only_fnum_shift(a, b) for a, b in pairs):
            shifted.extend(pairs)
        else:
            other.append((tag, old[i1:i2], new[j1:j2]))
    if other:
        tag, a, b = other[0]
        print(f"{spec}：Gherkin 行為與遷移前不同（共 {len(other)} 處），第一處（{tag}）：")
        for line in a[:5]:
            print(f"  - {line}")
        for line in b[:5]:
            print(f"  + {line}")
        return 1
    print(f"{spec}：Gherkin 只有 F 編號 ±1 的修正（{len(shifted)} 行），需審查輪確認是檔案參考的編號偏移：")
    for a, b in shifted[:5]:
        print(f"  - {a}\n  + {b}")
    return 3


def tag_diff(spec, opts):
    def strip(tags):
        return sorted(t for t in tags if not t.startswith(("@uc-", "@fail-")))

    old = {(f, n, k): strip(t) for f, n, k, t in feature_scenarios(old_text(spec, opts))}
    new = {(f, n, k): strip(t) for f, n, k, t in feature_scenarios(read(spec))}
    errors = []
    for key, tags in old.items():
        if key in new and new[key] != tags:
            errors.append(f"Scenario「{key[1]}」的 tag（不含 @uc-／@fail-）由 {tags} 變成 {new[key]}（不可增刪狀態 tag 與 @CR-）")
    return errors


def changelog_rows(text):
    rows, in_sec = [], False
    for line in text.split("\n"):
        if line.startswith("## "):
            in_sec = line.startswith("## 變更紀錄")
            continue
        s = line.strip()
        if not in_sec or not s.startswith("|"):
            continue
        cells = [c.strip() for c in s.strip("|").split("|")]
        if not re.fullmatch(r"\d{4}-\d{2}-\d{2}", cells[0]):
            continue
        rows.append((cells[0], cells[-1]))
    return rows


def norm_text(s):
    return re.sub(r"\s+", "", re.sub(r"[`「」]", "", s))


def changelog_check(spec, opts):
    new = changelog_rows(read(spec))
    errors = []
    for date, summary in changelog_rows(old_text(spec, opts)):
        want = norm_text(summary)
        if not any(d == date and want in norm_text(s) for d, s in new):
            errors.append(f"遷移前的變更紀錄（{date}）「{summary[:30]}…」在新檔找不到（摘要文字只能換引號、加註，不能刪改）")
    return errors


# ---------------------------------------------------------------- spec-check 包裝


def spec_files():
    return sorted(glob.glob(SPEC_GLOB))


def findings():
    cache = os.environ.get("FINDINGS_JSON")
    if cache and os.path.exists(cache):
        return json.loads(read(cache))
    r = subprocess.run([sys.executable, "scripts/spec-check", "--format", "json"], capture_output=True, text=True)
    try:
        data = json.loads(r.stdout)
    except json.JSONDecodeError:
        raise SystemExit(f"spec-check 執行失敗（非 JSON 輸出）：{r.stderr.strip()[:300]}")
    if cache:
        with open(cache, "w", encoding="utf-8") as f:
            json.dump(data, f, ensure_ascii=False)
    return data


def count(level, target, fs=None):
    fs = findings() if fs is None else fs
    return sum(1 for x in fs if x["level"] == level and (target == "all" or x["file"] == target))


def feature_range(spec, name):
    """以 gherkin 的 Feature 名稱找該 Feature 所在 H2 段落的行號範圍（1-based，含頭尾）。"""
    text = read(spec)
    lines = text.split("\n")
    blocks = gherkin_blocks(text)
    in_fence = set()
    for s, e, _ in blocks:
        in_fence.update(range(s, e + 1))
    h2 = [i for i, line in enumerate(lines) if line.startswith("## ") and i not in in_fence]
    for s, e, body in blocks:
        names = [normalize_line(b.strip())[len("Feature: "):] for b in body if b.strip().startswith("Feature:")]
        if name in names:
            start = max([i for i in h2 if i < s], default=0)
            end = min([i for i in h2 if i > e], default=len(lines))
            return start + 1, end
    return None


def eval_token(kind, arg, fs):
    """回傳錯誤訊息（None 代表條件成立）。"""
    if kind == "crcheck":
        base = read(BASELINE_FILE).strip()
        if not base:
            return f"crcheck({arg})：找不到 {BASELINE_FILE}"
        r = subprocess.run([sys.executable, "scripts/cr-check", "--base", base, "--cr", arg],
                           capture_output=True, text=True)
        last = (r.stdout.strip().split("\n") or [""])[-1]
        return None if r.returncode == 0 else f"crcheck({arg}) 未通過：{last}"
    m = re.fullmatch(r"(all|F\d{2})(?:#([^;]+))?(?:;([A-Z0-9,-]+))?", arg)
    if not m:
        return f"errors({arg})：條件格式無法解析"
    mod, feature, ids = m.groups()
    path = "all" if mod == "all" else spec_for_module(mod)
    if path is None:
        return f"errors({arg})：找不到模組 {mod} 的 spec 檔"
    sel = [x for x in fs if x["level"] == "error" and (path == "all" or x["file"] == path)]
    if feature:
        rng = feature_range(path, feature)
        if rng is None:
            return f"errors({arg})：{path} 找不到 Feature「{feature}」"
        sel = [x for x in sel if rng[0] <= x["line"] <= rng[1]]
    if ids:
        sel = [x for x in sel if x["check"] in ids.split(",")]
    if sel:
        first = sel[0]
        return f"errors({arg}) 應為 0，實際 {len(sel)}（例如 {first['file']}:{first['line']} {first['check']} {first['message']}）"
    return None


def accept_check(ledger, targets):
    rows = parse_ledger(ledger)
    if targets == ["--done"]:
        rows = [r for r in rows if r["status"] == "done"]
    else:
        rows = [r for r in rows if r["id"] in set(targets)]
    fs = None
    errors = []
    for r in rows:
        for kind, arg in TOKEN.findall(r["accept"]):
            if kind == "errors" and fs is None:
                fs = findings()
            msg = eval_token(kind, arg.strip(), fs)
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
            if r["status"] in {"blocked", "proposed"}:
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
        return report(check_append(args[0], args[1], "")[0])
    elif cmd == "check-oq":
        return report(check_oq(args[0], args[1]))
    elif cmd == "spec-files":
        print("\n".join(spec_files()))
    elif cmd == "legacy-path":
        print(legacy_path(args[0]))
    elif cmd == "gherkin-extract":
        print("\n".join(gherkin_extract(read(args[0]))))
    elif cmd == "gherkin-diff":
        return gherkin_diff(args[0], args[1:])
    elif cmd == "tag-diff":
        return report(tag_diff(args[0], args[1:]))
    elif cmd == "changelog-check":
        return report(changelog_check(args[0], args[1:]))
    elif cmd in ("error-count", "warn-count"):
        print(count("error" if cmd == "error-count" else "warn", args[0]))
    elif cmd == "errors-json":
        fs = findings()
        data = {p: count("error", p, fs) for p in spec_files()}
        data["all"] = count("error", "all", fs)
        print(json.dumps(data, ensure_ascii=False, indent=1))
    elif cmd == "accept-check":
        return report(accept_check(args[0], args[1:]))
    elif cmd == "crcheck":
        msg = eval_token("crcheck", args[0], [])
        print(msg or f"cr-check --cr {args[0]} 通過")
        return 1 if msg else 0
    elif cmd == "task-files":
        print("\n".join(f for r in parse_ledger(args[0]) if r["id"] == args[1] for f in task_files(r)))
    else:
        print(f"未知的子命令：{cmd}", file=sys.stderr)
        return 2
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
