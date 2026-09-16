"""解析 `.dev/CR.md` 總表（cr-convention.md §6）。"""
from __future__ import annotations

import os
import re

from . import md
from .ids import CR_RE
from .model import CR, Impact, Loc, Model

HEADER = ["編號", "標題", "類型", "提出人", "提出日期", "影響模組", "影響 ID", "狀態", "完成日期", "明細"]
TYPES = ["新增", "變更", "移除"]
STATUSES = ["記錄", "修改規格", "待處理", "處理完成", "駁回"]
IMPACT_RE = re.compile(r"^`([^`]+)`\s*(?:[（(](新增|移除)[)）])?\s*$")


def load_cr(model: Model, path: str) -> None:
    model.cr_path = path
    if not os.path.exists(path):
        model.cr_loaded = False
        return
    model.cr_loaded = True
    lines = md.read_lines(path)
    fences = md.find_fences(lines)
    fmap = md.in_fence_map(lines, fences)
    t = md.parse_table_at(lines, 0, fmap)
    # parse_table_at 遇到標題會停，總表前面有 H1，所以從第一個表格行開始找
    if t is None:
        for i, line in enumerate(lines):
            if fmap[i] is None and line.strip().startswith("|"):
                t = md.parse_table_at(lines, i, fmap)
                break
    if t is None:
        model.cr_format_errors.append((1, "找不到 CR 總表"))
        return
    if t.header != HEADER:
        model.cr_format_errors.append((t.start + 1, f"表頭應為 {HEADER}，實際 {t.header}"))
    col = {name: (t.header.index(name) if name in t.header else HEADER.index(name)) for name in HEADER}

    def cell(cells, name):
        i = col[name]
        return cells[i].strip() if i < len(cells) else ""

    seen: set[str] = set()
    for cells, li in t.rows:
        cid = cell(cells, "編號")
        if not cid:
            continue
        loc = Loc(path, li + 1)
        errs: list[str] = []
        if not CR_RE.match(cid):
            errs.append(f"編號「{cid}」格式應為 CR-<三位數>")
        if cid in seen:
            errs.append(f"編號「{cid}」重複")
        seen.add(cid)
        typ = cell(cells, "類型")
        if typ not in TYPES:
            errs.append(f"類型「{typ}」不在 {TYPES}")
        status = cell(cells, "狀態")
        if status not in STATUSES:
            errs.append(f"狀態「{status}」不在 {STATUSES}")
        impacts: list[Impact] = []
        raw_impact = cell(cells, "影響 ID")
        if raw_impact:
            for item in re.split(r"[、,]", raw_impact):
                item = item.strip()
                if not item:
                    continue
                m = IMPACT_RE.match(item)
                if not m:
                    errs.append(f"影響 ID 項目「{item}」格式應為 `id` 或 `id`(新增)/(移除)")
                    continue
                impacts.append(Impact(id=m.group(1).strip(), action=m.group(2) or ""))
        model.crs.append(
            CR(
                id=cid,
                title=cell(cells, "標題"),
                type=typ,
                proposer=cell(cells, "提出人"),
                date=cell(cells, "提出日期"),
                modules=[x.strip() for x in re.split(r"[、,]", cell(cells, "影響模組")) if x.strip()],
                impact=impacts,
                status=status,
                done_date=cell(cells, "完成日期"),
                detail=cell(cells, "明細"),
                loc=loc,
                format_errors=errs,
            )
        )
