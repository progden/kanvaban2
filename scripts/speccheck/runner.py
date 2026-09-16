"""三支腳本共用的 CLI 骨架：建模型、跑檢查、輸出。"""
from __future__ import annotations

import argparse
import glob
import json
import os
import subprocess
import sys
import tempfile
from typing import Iterable, Optional

from . import checks
from .model import Finding, Model
from .parser_cr import load_cr
from .parser_design import parse_design
from .parser_spec import parse_spec

DEFAULT_SPEC_GLOB = ".dev/F*/spec-*.md"
DEFAULT_DESIGN_GLOB = ".dev/F*/ui-*.md"
DEFAULT_CR = ".dev/CR.md"


def expand(patterns: Iterable[str]) -> list[str]:
    out: list[str] = []
    for p in patterns:
        if os.path.isfile(p):
            out.append(p)
        else:
            out.extend(sorted(glob.glob(p, recursive=True)))
    # 去重、保序
    seen: set[str] = set()
    return [x for x in out if not (x in seen or seen.add(x))]


def build_model(spec_paths: list[str], design_paths: list[str], cr_path: Optional[str]) -> Model:
    model = Model()
    for p in spec_paths:
        model.specs.append(parse_spec(p))
    for p in design_paths:
        model.designs.append(parse_design(p))
    if cr_path:
        load_cr(model, cr_path)
    return model


def specs_for_designs(design_paths: list[str]) -> list[str]:
    """ui-<模組>.md 對應同目錄的 spec-<模組>.md。"""
    out = []
    for d in design_paths:
        base = os.path.basename(d)
        if base.startswith("ui-"):
            s = os.path.join(os.path.dirname(d), "spec-" + base[len("ui-"):])
            if os.path.isfile(s):
                out.append(s)
    return out


def emit(findings: list[Finding], fmt: str) -> int:
    """輸出 findings，回傳 exit code。"""
    errors = sum(1 for f in findings if f.level == "error")
    warns = sum(1 for f in findings if f.level == "warn")
    if fmt == "json":
        print(json.dumps([f.__dict__ for f in findings], ensure_ascii=False, indent=2))
    else:
        for f in findings:
            print(f.format())
        print(f"{errors} error(s), {warns} warning(s)")
    return 1 if errors else 0


def common_parser(prog: str, desc: str) -> argparse.ArgumentParser:
    ap = argparse.ArgumentParser(prog=prog, description=desc)
    ap.add_argument("--format", choices=["text", "json"], default="text", help="輸出格式")
    ap.add_argument("--cr-file", default=DEFAULT_CR, help="CR 總表路徑（預設 .dev/CR.md）")
    return ap


# ---------------------------------------------------------------- spec-check


def main_spec(argv: list[str]) -> int:
    ap = common_parser("spec-check", "檢查 spec-<模組>.md（REF／UC／GH）")
    ap.add_argument("paths", nargs="*", help=f"spec 檔或 glob（預設 {DEFAULT_SPEC_GLOB}）")
    ap.add_argument("--ui", action="append", default=[], help="ui 檔 glob，只供 --report 印 Screen 欄")
    ap.add_argument("--report", action="store_true", help="印出 CRUD／角色／事件／追溯矩陣")
    ap.add_argument("--cr", help="--report 時只印該 CR 影響的列")
    args = ap.parse_args(argv)

    specs = expand(args.paths or [DEFAULT_SPEC_GLOB])
    if not specs:
        print("找不到任何 spec 檔", file=sys.stderr)
        return 2
    designs = expand(args.ui)
    model = build_model(specs, designs, args.cr_file)
    if args.report:
        from .report import spec_report

        print(spec_report(model, only_cr=args.cr))
        return 0
    return emit(checks.run(model, "spec-check"), args.format)


# ---------------------------------------------------------------- ui-check


def main_ui(argv: list[str]) -> int:
    ap = common_parser("ui-check", "檢查 ui-<模組>.md（REF-07／DS）")
    ap.add_argument("paths", nargs="*", help=f"ui 檔或 glob（預設 {DEFAULT_DESIGN_GLOB}）")
    ap.add_argument("--spec", action="append", default=[], help="額外的 spec 檔 glob（預設自動找同目錄同模組的 spec）")
    ap.add_argument("--report", action="store_true", help="印出畫面總表與追溯矩陣")
    args = ap.parse_args(argv)

    designs = expand(args.paths or [DEFAULT_DESIGN_GLOB])
    if not designs:
        print("找不到任何 ui 檔", file=sys.stderr)
        return 2
    specs = expand(args.spec) if args.spec else []
    specs = list(dict.fromkeys(specs_for_designs(designs) + specs))
    model = build_model(specs, designs, args.cr_file)
    if args.report:
        from .report import design_report

        print(design_report(model))
        return 0
    return emit(checks.run(model, "ui-check"), args.format)


# ---------------------------------------------------------------- cr-check


def _git(repo: str, *args: str) -> str:
    return subprocess.run(["git", "-C", repo, *args], check=True, capture_output=True, text=True).stdout


def _repo_root(start: str) -> str:
    return _git(start, "rev-parse", "--show-toplevel").strip()


def _changed_ranges(repo: str, base: str, head: Optional[str], paths: list[str]) -> dict[str, list[tuple[int, int]]]:
    """用 git diff 取得每個檔案在 head 版本被改動的行號區間（新增／修改）。刪除的行折算到刪除點那一行。"""
    args = ["diff", "--unified=0", "--no-color", f"{base}...{head}" if head else base, "--"] + paths
    try:
        text = _git(repo, *args)
    except subprocess.CalledProcessError:
        # 三點語法在沒有共同祖先時會失敗，退回兩點
        args[3] = f"{base}..{head}" if head else base
        text = _git(repo, *args)
    out: dict[str, list[tuple[int, int]]] = {}
    current = None
    for line in text.split("\n"):
        if line.startswith("+++ "):
            p = line[4:].strip()
            current = p[2:] if p.startswith("b/") else (None if p == "/dev/null" else p)
        elif line.startswith("@@") and current:
            # @@ -a,b +c,d @@
            plus = line.split("+", 1)[1].split(" ", 1)[0]
            start, _, cnt = plus.partition(",")
            s = int(start)
            n = int(cnt) if cnt else 1
            out.setdefault(current, []).append((s, max(s, s + n - 1)))
    return out


def _load_base_model(repo: str, base: str, rel_specs: list[str], rel_designs: list[str], rel_cr: Optional[str]) -> Model:
    """把 base 版本的檔案寫到暫存目錄再解析，模型裡的路徑改回 repo 相對路徑。"""
    tmp = tempfile.mkdtemp(prefix="cr-check-base-")
    mapping: dict[str, str] = {}

    def fetch(rel: str) -> Optional[str]:
        try:
            content = _git(repo, "show", f"{base}:{rel}")
        except subprocess.CalledProcessError:
            return None
        dst = os.path.join(tmp, rel)
        os.makedirs(os.path.dirname(dst), exist_ok=True)
        with open(dst, "w", encoding="utf-8") as f:
            f.write(content)
        mapping[dst] = rel
        return dst

    specs = [p for p in (fetch(r) for r in rel_specs) if p]
    designs = [p for p in (fetch(r) for r in rel_designs) if p]
    cr = fetch(rel_cr) if rel_cr else None
    m = build_model(specs, designs, cr)
    # 路徑正規化回相對路徑
    for s in m.specs:
        _rewrite_paths(s, mapping)
    for d in m.designs:
        _rewrite_paths(d, mapping)
    return m


def _rewrite_paths(obj, mapping: dict[str, str]) -> None:
    """把模型物件裡所有 Loc.file 換成相對路徑（Loc 是 frozen dataclass，整個換掉）。"""
    from dataclasses import fields, is_dataclass, replace

    from .model import Loc

    def walk(x):
        if isinstance(x, Loc):
            return replace(x, file=mapping.get(x.file, x.file))
        if is_dataclass(x) and not isinstance(x, type):
            for f in fields(x):
                setattr(x, f.name, walk(getattr(x, f.name)))
            return x
        if isinstance(x, list):
            return [walk(i) for i in x]
        if isinstance(x, dict):
            return {k: walk(v) for k, v in x.items()}
        if isinstance(x, set):
            return x
        return x

    walk(obj)
    if hasattr(obj, "path"):
        obj.path = mapping.get(obj.path, obj.path)


def main_cr(argv: list[str]) -> int:
    from .checks.cr import DiffContext

    ap = common_parser("cr-check", "比對 CR 影響 ID 與 PR diff（CR／GH-05）")
    ap.add_argument("--base", required=True, help="PR base 的 git ref")
    ap.add_argument("--head", default=None, help="head 的 git ref（預設工作區）")
    ap.add_argument("--cr", action="append", default=[], help="明確指定要比對的 CR 編號，可多次")
    ap.add_argument("--repo", default=".", help="repo 內任一路徑（預設目前目錄）")
    ap.add_argument("--spec-glob", default=DEFAULT_SPEC_GLOB)
    ap.add_argument("--ui-glob", default=DEFAULT_DESIGN_GLOB)
    args = ap.parse_args(argv)

    root = _repo_root(args.repo)
    cwd = os.getcwd()
    os.chdir(root)
    try:
        specs = expand([args.spec_glob])
        designs = expand([args.ui_glob])
        cr_rel = args.cr_file if os.path.isfile(args.cr_file) else args.cr_file
        model = build_model(specs, designs, cr_rel)
        base_model = _load_base_model(root, args.base, specs, designs, cr_rel)
        changed = _changed_ranges(root, args.base, args.head, specs + designs + [cr_rel])
        model.diff = DiffContext(base=base_model, changed=changed, explicit_crs=list(args.cr), rel={p: p for p in specs + designs})
        findings = checks.run(model, "cr-check")
    finally:
        os.chdir(cwd)
    return emit(findings, args.format)
