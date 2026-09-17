#!/usr/bin/env python3
"""驗證 OQ 表與 ui-*.md「待確認事項」裡標【引用原文】／【矛盾】／【推論】／【覆蓋】的
『』逐字引用，是否真的能在對應 spec-<模組>.md 裡找到完全相同的子字串。

只做純文字比對（去除多餘空白後的子字串搜尋），不依賴 scripts/speccheck 的 parser，
也不修改 scripts/** 底下任何檔案。屬於 ui-authoring-loop 自己的收尾檢查腳本。
"""
import re
import sys
from pathlib import Path

SCRIPT_DIR = Path(__file__).resolve().parent
LOOP_DIR = SCRIPT_DIR.parent
REPO_ROOT = LOOP_DIR.parent.parent.parent
DEV_DIR = REPO_ROOT / ".dev"
OQ_FILE = LOOP_DIR / ".state" / "ui-authoring-open-questions.md"

QUOTE_RE = re.compile(r"『(.*?)』")
TAG_RE = re.compile(r"【(引用原文|矛盾|推論|覆蓋)】")
ROW_ID_RE = re.compile(r"^\|\s*(OQ-\d+)\s*\|")
SUPERSEDED_RE = re.compile(r"推翻\s*(OQ-\d+)")


def normalize(text: str) -> str:
    """把連續空白／換行壓成單一空白，方便逐字比對時忽略排版差異。"""
    return re.sub(r"\s+", " ", text).strip()


def discover_modules() -> dict:
    """回傳 {模組短名: spec 檔路徑} 的對照表，模組短名取自 .dev/F??-<名稱> 目錄名去掉編號。"""
    modules = {}
    for d in sorted(DEV_DIR.glob("F[0-9][0-9]-*")):
        if not d.is_dir():
            continue
        short_name = d.name.split("-", 1)[1]
        spec_files = list(d.glob("spec-*.md"))
        if spec_files:
            modules[short_name] = spec_files[0]
    return modules


def spec_for_ui_file(ui_path: Path, modules: dict) -> Path | None:
    """ui-<模組>.md 依檔名直接對應同目錄下的 spec-<模組>.md。"""
    candidate = ui_path.parent / ui_path.name.replace("ui-", "spec-", 1)
    if candidate.exists():
        return candidate
    return None


def spec_for_line(line: str, modules: dict) -> Path | None:
    """OQ 表沒有檔名可用，靠「模組」欄或行內文字比對已知模組短名。"""
    for short_name, spec_path in modules.items():
        if short_name in line or spec_path.name in line:
            return spec_path
    return None


def quotes_to_check(tag: str, line: str) -> list[str]:
    """【覆蓋】只驗證「→」之前的原文，之後的新文本來就不該存在於 spec。"""
    if tag == "覆蓋" and "→" in line:
        before = line.split("→", 1)[0]
        return QUOTE_RE.findall(before)
    return QUOTE_RE.findall(line)


def table_body_lines(lines: list[str]) -> set[int]:
    """OQ 檔開頭是規則說明文字，範例裡也會出現【引用原文】等標籤字樣；
    只有表格標題列（「| 編號」開頭）之後的資料列才是真正要驗證的內容。"""
    body = set()
    in_table = False
    for i, line in enumerate(lines, start=1):
        if line.strip().startswith("| 編號"):
            in_table = True
            continue
        if in_table and line.strip().startswith("|"):
            body.add(i)
    return body


def superseded_oq_ids(lines: list[str], body_lines: set[int]) -> set[str]:
    """掃過 OQ 表資料列，收集所有『推翻 OQ-xx』提到的編號——這些舊列的引用允許因為後續
    人工直接修正 spec 而不再逐字相符（歷史記錄，不可修改，但也不用再驗證）。"""
    ids = set()
    for lineno in body_lines:
        ids.update(SUPERSEDED_RE.findall(lines[lineno - 1]))
    return ids


def check_file(path: Path, modules: dict, is_ui_file: bool):
    violations = []
    text = path.read_text(encoding="utf-8")
    lines = text.splitlines()
    allowed_lines = set(range(1, len(lines) + 1)) if is_ui_file else table_body_lines(lines)
    fixed_spec = spec_for_ui_file(path, modules) if is_ui_file else None
    superseded = set() if is_ui_file else superseded_oq_ids(lines, allowed_lines)
    for lineno, line in enumerate(lines, start=1):
        if lineno not in allowed_lines:
            continue
        if not is_ui_file:
            row_id = ROW_ID_RE.match(line)
            if row_id and row_id.group(1) in superseded:
                continue
        tag_match = TAG_RE.search(line)
        if not tag_match:
            continue
        tag = tag_match.group(1)
        quotes = quotes_to_check(tag, line)
        if not quotes:
            violations.append((lineno, tag, None, "找不到『』逐字引用，違反格式要求"))
            continue
        spec_path = fixed_spec or spec_for_line(line, modules)
        if spec_path is None:
            violations.append((lineno, tag, None, "無法判斷對應的 spec-<模組>.md，無法驗證"))
            continue
        spec_text = normalize(spec_path.read_text(encoding="utf-8"))
        for quote in quotes:
            if normalize(quote) not in spec_text:
                violations.append(
                    (lineno, tag, quote, f"在 {spec_path.relative_to(REPO_ROOT)} 找不到逐字相符的原文")
                )
    return violations


def main():
    modules = discover_modules()
    if not modules:
        print("找不到任何 .dev/F??-*/spec-*.md，無法比對，視為失敗")
        return 1

    targets = []
    if OQ_FILE.exists():
        targets.append((OQ_FILE, False))
    for ui_path in sorted(DEV_DIR.glob("F[0-9][0-9]-*/ui-*.md")):
        targets.append((ui_path, True))

    all_violations = []
    for path, is_ui_file in targets:
        for lineno, tag, quote, reason in check_file(path, modules, is_ui_file):
            all_violations.append((path, lineno, tag, quote, reason))

    if not all_violations:
        print("逐字引用驗證通過：所有【引用原文】／【矛盾】／【推論】／【覆蓋】的『』引用皆能在對應 spec 找到")
        return 0

    print(f"逐字引用驗證失敗，共 {len(all_violations)} 筆：")
    for path, lineno, tag, quote, reason in all_violations:
        rel = path.relative_to(REPO_ROOT)
        quote_repr = f"『{quote}』" if quote else "(無引用)"
        print(f"  {rel}:{lineno} 【{tag}】{quote_repr} — {reason}")
    return 1


if __name__ == "__main__":
    sys.exit(main())
