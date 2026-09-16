"""檢查函式登錄表。

每條檢查是 `(Model) -> list[Finding]` 的純函式，用 `@check(...)` 登記 ID、所屬腳本與層級。
`checks.md` 的每個 ID 都要在這裡出現一次；`registry_matches_doc()` 會核對。
"""
from __future__ import annotations

from dataclasses import dataclass
from typing import Callable

from ..model import Finding, Loc, Model

CheckFn = Callable[[Model], list[Finding]]


@dataclass
class Check:
    id: str
    scripts: tuple[str, ...]  # 涵蓋此檢查的腳本名稱
    level: str
    fn: CheckFn
    desc: str


REGISTRY: dict[str, Check] = {}


def check(id: str, scripts: str | tuple[str, ...], level: str, desc: str = ""):
    """登記一條檢查。scripts 可以是單一名稱或多個（REF-08、GH-05 由兩支腳本分工）。"""
    if isinstance(scripts, str):
        scripts = (scripts,)

    def deco(fn: CheckFn) -> CheckFn:
        if id in REGISTRY:
            raise RuntimeError(f"檢查 {id} 重複登記")
        REGISTRY[id] = Check(id=id, scripts=scripts, level=level, fn=fn, desc=desc)
        return fn

    return deco


def finding(loc: Loc, level: str, id: str, message: str) -> Finding:
    return Finding(file=loc.file, line=loc.line, level=level, check=id, message=message)


def run(model: Model, script: str) -> list[Finding]:
    """跑某支腳本涵蓋的所有檢查，依檔案、行號、檢查 ID 排序。"""
    out: list[Finding] = []
    for c in REGISTRY.values():
        if script in c.scripts:
            out.extend(c.fn(model))
    out.sort(key=lambda f: (f.file, f.line, f.check, f.message))
    return out


def ids_for(script: str) -> set[str]:
    return {c.id for c in REGISTRY.values() if script in c.scripts}


# 匯入各組檢查以觸發登記
from . import cr, ds, gh, ref, uc  # noqa: E402,F401
