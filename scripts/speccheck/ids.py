"""ID 形式判定（spec-convention.md §9）。"""
from __future__ import annotations

import re

SLUG = r"[a-z0-9]+(?:-[a-z0-9]+)*"
ENTITY_RE = re.compile(rf"^{SLUG}$")
ATTR_RE = re.compile(rf"^({SLUG})\.({SLUG})$")
ROLE_RE = re.compile(rf"^r-{SLUG}$")
UC_RE = re.compile(rf"^uc-{SLUG}$")
EV_RE = re.compile(rf"^ev-{SLUG}$")
SCREEN_RE = re.compile(rf"^s-{SLUG}$")
CR_RE = re.compile(r"^CR-\d{3}$")

RESERVED_PREFIXES = ("r-", "uc-", "ev-", "s-")

KIND_LABEL = {
    "entity": "Entity",
    "attribute": "Attribute",
    "role": "Role",
    "usecase": "UseCase",
    "event": "Event",
    "screen": "Screen",
    "invalid": "不合法",
    "path": "檔案路徑",
}


def kind_of(raw: str) -> str:
    """依前綴判斷反引號內容是哪一種 ID；不符合任何形式回 "invalid"。"""
    s = raw.strip()
    if "/" in s or s.endswith(".md"):
        return "path"
    if ROLE_RE.match(s):
        return "role"
    if UC_RE.match(s):
        return "usecase"
    if EV_RE.match(s):
        return "event"
    if SCREEN_RE.match(s):
        return "screen"
    if ATTR_RE.match(s):
        return "attribute"
    if ENTITY_RE.match(s) and not s.startswith(RESERVED_PREFIXES):
        return "entity"
    return "invalid"


def is_valid_entity_id(s: str) -> bool:
    return bool(ENTITY_RE.match(s)) and not s.startswith(RESERVED_PREFIXES) and "." not in s


def split_attr(s: str) -> tuple[str, str]:
    m = ATTR_RE.match(s)
    if not m:
        return s, ""
    return m.group(1), m.group(2)
