"""解析 `ui-<模組>.md`（scripts.md §3.1）。"""
from __future__ import annotations

import re

from . import md
from .ids import kind_of
from .model import DesignFile, Loc, Ref, Screen, TableRow

SCREEN_H2_RE = re.compile(r"^(s-[a-z0-9]+(?:-[a-z0-9]+)*)：(.+?)\s*$")
SECTIONS = ["目的", "進入與離開", "角色與權限", "資料", "操作", "狀態", "驗收條件", "待確認事項"]
TYPES = ["列表", "詳情", "表單", "儀表板", "流程", "對話框", "設定"]
STATUSES = ["未討論", "討論中", "已定案"]
STATE_ITEMS = ["載入中", "空資料", "錯誤", "無權限", "資料狀態差異"]
NAV_ITEMS = {"從哪裡進來": "from", "完成後去哪裡": "to", "中途放棄會怎樣": "abort"}
BULLET_RE = re.compile(r"^\s*[-*]\s*(.+?)[：:]\s*(.*)$")
FORBIDDEN_H = [("實體", 3), ("欄位", 3), ("關係", 3), ("角色定義", 2), ("名詞定義", 2), ("Use Case 定義", 3)]


def parse_design(path: str) -> DesignFile:
    lines = md.read_lines(path)
    fences = md.find_fences(lines)
    fmap = md.in_fence_map(lines, fences)
    headings = md.find_headings(lines, fmap)
    design = DesignFile(path=path, module=md.module_of(path, "ui"))

    for f in fences:
        if f.lang == "usecase":
            design.forbidden_sections.append((f.start + 1, "```usecase 區塊"))
    for h in headings:
        for fixed, lvl in FORBIDDEN_H:
            if h.level == lvl and md.heading_matches(h.text, fixed):
                design.forbidden_sections.append((h.index + 1, f"{'#' * lvl} {fixed}"))

    h2s = [h for h in headings if h.level == 2]
    for n, h in enumerate(h2s):
        end = h2s[n + 1].index if n + 1 < len(h2s) else len(lines)
        m = SCREEN_H2_RE.match(h.text)
        if not m:
            design.structure_errors.append((h.index + 1, f"H2 標題「{h.text}」不符合「## s-<id>：<畫面名稱>」"))
            continue
        sc = Screen(id=m.group(1), name=m.group(2).strip(), module=design.module, loc=Loc(path, h.index + 1), end_line=end)
        design.screens.append(sc)
        _parse_header(sc, lines, h.index + 1, end)
        _parse_sections(design, sc, lines, fmap, headings, h.index, end)

    for i, line in md.iter_prose_lines(lines, fmap):
        for raw in md.backticks(line):
            design.refs.append(Ref(id=raw.strip(), kind=kind_of(raw), loc=Loc(path, i + 1), context="ui 正文"))
    return design


def _parse_header(sc: Screen, lines, start: int, end: int) -> None:
    expect = [("所屬 Feature", "feature"), ("類型", "type"), ("狀態", "status")]
    i = start
    for label, attr in expect:
        while i < end and not lines[i].strip():
            i += 1
        if i >= end:
            sc.header_errors.append(f"缺少「{label}：」行")
            continue
        m = re.match(rf"^{re.escape(label)}[：:]\s*(.*?)\s*$", lines[i].strip())
        if not m:
            sc.header_errors.append(f"第 {i + 1} 行應為「{label}：…」，實際「{lines[i].strip()}」")
            continue
        setattr(sc, attr, m.group(1))
        i += 1
    if sc.type is not None and sc.type not in TYPES:
        sc.header_errors.append(f"類型「{sc.type}」不在 {TYPES}")
    if sc.status is not None and sc.status not in STATUSES:
        sc.header_errors.append(f"狀態「{sc.status}」不在 {STATUSES}")


def _parse_sections(design: DesignFile, sc: Screen, lines, fmap, headings, start: int, end: int) -> None:
    h3s = [h for h in headings if h.level == 3 and start < h.index < end]
    for n, h in enumerate(h3s):
        sec_end = h3s[n + 1].index if n + 1 < len(h3s) else end
        name = next((s for s in SECTIONS if md.heading_matches(h.text, s)), None)
        if name is None:
            continue
        sc.section_order.append(name)
        sc.sections[name] = Loc(design.path, h.index + 1)
        if name == "資料":
            sc.data = _rows(design, lines, fmap, h.index)
            sc.col_refs["資料表.來源"] = _col_refs(design, sc.data, "來源", 1)
        elif name == "操作":
            sc.ops = _rows(design, lines, fmap, h.index)
            sc.col_refs["操作表.觸發"] = _col_refs(design, sc.ops, "觸發", 1)
        elif name == "角色與權限":
            sc.role_rows = _rows(design, lines, fmap, h.index)
            sc.col_refs["角色表.角色"] = _col_refs(design, sc.role_rows, "角色", 0)
        elif name == "進入與離開":
            _parse_nav(design, sc, lines, h.index + 1, sec_end)
        elif name == "狀態":
            _parse_states(sc, lines, h.index + 1, sec_end)


def _rows(design: DesignFile, lines, fmap, idx: int) -> list[TableRow]:
    t = md.parse_table_at(lines, idx + 1, fmap)
    if not t:
        return []
    rows = [TableRow(cells=cells, loc=Loc(design.path, li + 1)) for cells, li in t.rows]
    # 把表頭存在第一列前面，方便依名稱找欄位
    rows.insert(0, TableRow(cells=t.header, loc=Loc(design.path, t.start + 1)))
    return rows


def _col_refs(design: DesignFile, rows: list[TableRow], col_name: str, default_idx: int) -> list[Ref]:
    if not rows:
        return []
    header = rows[0].cells
    idx = header.index(col_name) if col_name in header else default_idx
    out: list[Ref] = []
    for r in rows[1:]:
        cell = r.cells[idx] if idx < len(r.cells) else ""
        for raw in md.backticks(cell):
            out.append(Ref(id=raw.strip(), kind=kind_of(raw), loc=r.loc, context=col_name))
    return out


def _parse_nav(design: DesignFile, sc: Screen, lines, start: int, end: int) -> None:
    for i in range(start, end):
        m = BULLET_RE.match(lines[i])
        if not m:
            continue
        key = NAV_ITEMS.get(m.group(1).strip())
        if key is None:
            continue
        text = m.group(2)
        refs = [Ref(id=r.strip(), kind=kind_of(r), loc=Loc(design.path, i + 1), context="進入與離開") for r in md.backticks(text)]
        sc.nav_refs.extend(refs)
        ids = [r.id for r in refs]
        if key == "from":
            sc.nav_from.extend(ids)
            if "模組入口" in text:
                sc.is_entry = True
        else:
            sc.nav_to.extend(ids)


def _parse_states(sc: Screen, lines, start: int, end: int) -> None:
    for i in range(start, end):
        m = BULLET_RE.match(lines[i])
        if not m:
            continue
        key = m.group(1).strip()
        if key in STATE_ITEMS:
            sc.states[key] = m.group(2).strip()
