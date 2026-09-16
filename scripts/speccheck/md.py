"""Markdown 解析小工具：程式碼圍欄、標題、表格、反引號。

只支援本規範用到的子集，不是完整的 Markdown parser。
"""
from __future__ import annotations

import re
from dataclasses import dataclass
from typing import Iterator, Optional

FENCE_RE = re.compile(r"^(`{3,}|~{3,})\s*([\w-]*)\s*$")
HEADING_RE = re.compile(r"^(#{1,6})\s+(.*?)\s*#*\s*$")
BACKTICK_RE = re.compile(r"`([^`\n]+)`")
# 固定字串標題後面只允許：行尾、空白、全形／半形括號、斜線
PREFIX_TAIL_RE = r"(?:$|\s|（|\(|/)"


@dataclass
class Fence:
    lang: str
    start: int  # 開頭圍欄那一行的 index（0-based）
    end: int  # 結尾圍欄那一行的 index；沒關閉則為 len(lines)
    body_start: int
    body_end: int  # exclusive


def read_lines(path: str) -> list[str]:
    with open(path, encoding="utf-8") as f:
        return f.read().split("\n")


def find_fences(lines: list[str]) -> list[Fence]:
    """找出所有程式碼圍欄。四個反引號可以包三個反引號（規範文件會這樣寫）。"""
    fences: list[Fence] = []
    i = 0
    while i < len(lines):
        m = FENCE_RE.match(lines[i])
        if not m:
            i += 1
            continue
        marker, lang = m.group(1), m.group(2)
        j = i + 1
        while j < len(lines):
            m2 = FENCE_RE.match(lines[j])
            # 結尾圍欄：同字元、長度 >= 開頭、沒有語言標記
            if m2 and m2.group(1)[0] == marker[0] and len(m2.group(1)) >= len(marker) and not m2.group(2):
                break
            j += 1
        fences.append(Fence(lang=lang, start=i, end=j, body_start=i + 1, body_end=j))
        i = j + 1
    return fences


def in_fence_map(lines: list[str], fences: list[Fence]) -> list[Optional[Fence]]:
    """每一行對應到它所在的圍欄（含圍欄行本身），不在圍欄裡則 None。"""
    out: list[Optional[Fence]] = [None] * len(lines)
    for f in fences:
        for k in range(f.start, min(f.end + 1, len(lines))):
            out[k] = f
    return out


@dataclass
class Heading:
    level: int
    text: str
    index: int  # 0-based 行 index


def find_headings(lines: list[str], fence_map: list[Optional[Fence]]) -> list[Heading]:
    out = []
    for i, line in enumerate(lines):
        if fence_map[i] is not None:
            continue
        m = HEADING_RE.match(line)
        if m:
            out.append(Heading(level=len(m.group(1)), text=m.group(2).strip(), index=i))
    return out


def heading_matches(text: str, fixed: str) -> bool:
    """固定字串標題以前綴比對；固定字串後只能接行尾、空白、括號或斜線。"""
    return re.match(re.escape(fixed) + PREFIX_TAIL_RE, text) is not None


@dataclass
class Table:
    header: list[str]
    rows: list[tuple[list[str], int]]  # (cells, 0-based 行 index)
    start: int
    end: int  # exclusive


def split_row(line: str) -> list[str]:
    """把 `| a | b |` 切成 cells，去頭尾空白。反引號裡的 `|` 不處理（規範不會這樣寫）。"""
    s = line.strip()
    if s.startswith("|"):
        s = s[1:]
    if s.endswith("|"):
        s = s[:-1]
    return [c.strip() for c in s.split("|")]


def is_separator_row(cells: list[str]) -> bool:
    return bool(cells) and all(re.fullmatch(r":?-{1,}:?", c) for c in cells)


def parse_table_at(lines: list[str], start: int, fence_map: list[Optional[Fence]]) -> Optional[Table]:
    """從 start 開始找第一個表格（跳過空行與非表格行直到遇到下一個標題）。"""
    i = start
    while i < len(lines):
        if fence_map[i] is None and HEADING_RE.match(lines[i]):
            return None
        if fence_map[i] is None and lines[i].strip().startswith("|"):
            break
        i += 1
    if i >= len(lines):
        return None
    header = split_row(lines[i])
    j = i + 1
    if j < len(lines) and is_separator_row(split_row(lines[j])):
        j += 1
    rows: list[tuple[list[str], int]] = []
    while j < len(lines) and lines[j].strip().startswith("|") and fence_map[j] is None:
        rows.append((split_row(lines[j]), j))
        j += 1
    return Table(header=header, rows=rows, start=i, end=j)


def backticks(text: str) -> list[str]:
    """回傳單反引號裡的內容清單。"""
    return BACKTICK_RE.findall(text)


def iter_prose_lines(lines: list[str], fence_map: list[Optional[Fence]]) -> Iterator[tuple[int, str]]:
    """走訪所有不在程式碼圍欄裡的行。"""
    for i, line in enumerate(lines):
        if fence_map[i] is None:
            yield i, line


def module_of(path: str, prefix: str) -> str:
    """從 `spec-<模組>.md` 或 `ui-<模組>.md` 取出模組名。"""
    import os

    base = os.path.basename(path)
    if base.startswith(prefix + "-") and base.endswith(".md"):
        return base[len(prefix) + 1 : -3]
    return base[:-3] if base.endswith(".md") else base
