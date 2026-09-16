"""CR：變更單（cr-check 專用，需要 PR diff）。"""
from __future__ import annotations

from dataclasses import dataclass, field
from typing import Optional

from ..model import Finding, Loc, Model
from . import check, finding


@dataclass
class DiffContext:
    """cr-check 建好後掛在 model.diff 上。"""

    base: Model  # base 版本的模型（檔案路徑已正規化為 repo 相對路徑）
    changed: dict[str, list[tuple[int, int]]] = field(default_factory=dict)  # repo 相對路徑 -> head 行號區間（1-based, inclusive）
    explicit_crs: list[str] = field(default_factory=list)
    rel: dict[str, str] = field(default_factory=dict)  # head 檔案路徑 -> repo 相對路徑


def _rel(ctx: DiffContext, path: str) -> str:
    return ctx.rel.get(path, path)


def _touched(ctx: DiffContext, path: str, start: int, end: int) -> bool:
    for a, b in ctx.changed.get(_rel(ctx, path), []):
        if a <= end and start <= b:
            return True
    return False


def _id_ranges(model: Model, ctx: Optional[DiffContext]) -> dict[str, tuple[str, int, int]]:
    """ID -> (repo 相對路徑, 起, 迄)。Scenario 折算到它的 uc。"""
    out: dict[str, tuple[str, int, int]] = {}

    def rel(p: str) -> str:
        return _rel(ctx, p) if ctx else p

    for spec in model.specs:
        for eid, (a, b) in spec.entity_loc_range.items():
            out[eid] = (rel(spec.path), a, b)
        for u in spec.usecases:
            out[u.id] = (rel(spec.path), u.loc.line, u.end_line)
    for d in model.designs:
        for s in d.screens:
            out[s.id] = (rel(d.path), s.loc.line, s.end_line)
    return out


def changed_ids(model: Model) -> tuple[set[str], set[str]]:
    """回傳 (全部被改動的 ID, 其中屬於「新增或移除」的 ID)。"""
    ctx: DiffContext = model.diff
    head = _id_ranges(model, ctx)
    base = _id_ranges(ctx.base, None)
    changed: set[str] = set()
    for id_, (path, a, b) in head.items():
        if id_ not in base or _touched(ctx, path, a, b):
            changed.add(id_)
    for id_ in base:
        if id_ not in head:
            changed.add(id_)
    # Scenario 被改 -> 歸到它的 uc
    for spec in model.specs:
        for sc in spec.scenarios:
            if sc.uc and _touched(ctx, spec.path, sc.loc.line, sc.end_line):
                changed.add(sc.uc)
            if sc.uc and sc.tag_loc and _touched(ctx, spec.path, sc.tag_loc.line, sc.loc.line):
                changed.add(sc.uc)
    added_removed = {i for i in changed if (i in head) != (i in base)}
    return changed, added_removed


def _involved_crs(model: Model) -> list[str]:
    ctx: DiffContext = model.diff
    base_crs = {c for sc in ctx.base.scenarios for c in sc.crs}
    head_crs = {c for sc in model.scenarios for c in sc.crs}
    return sorted((head_crs - base_crs) | set(ctx.explicit_crs))


def _cr_loc(model: Model, cr_id: str) -> Loc:
    for c in model.crs:
        if c.id == cr_id:
            return c.loc
    return Loc(model.cr_path or ".dev/CR.md", 1)


@check("CR-01", "cr-check", "error", "影響 ID 與 diff 一致")
def cr_01(model: Model) -> list[Finding]:
    if model.diff is None:
        return []
    out = []
    changed, _ = changed_ids(model)
    crs = {c.id: c for c in model.crs}
    involved = _involved_crs(model)
    # 多張 CR 同一個 PR 時，影響 ID 聯集對照 diff
    union: set[str] = set()
    for cid in involved:
        c = crs.get(cid)
        if c is None:
            out.append(finding(_cr_loc(model, cid), "error", "CR-01", f"{cid} 影響 ID 與 diff 不符：{cid} 未在總表登記"))
            continue
        union |= {i.id for i in c.impact}
    if not involved:
        return out
    extra = sorted(changed - union)
    missing = sorted(union - changed)
    if extra or missing:
        label = "、".join(involved)
        out.append(
            finding(
                _cr_loc(model, involved[0]),
                "error",
                "CR-01",
                f"{label} 影響 ID 與 diff 不符：diff 多改 {extra or '無'}；CR 列了但未改 {missing or '無'}",
            )
        )
    return out


@check("CR-02", "cr-check", "error", "同一 Scenario 不被兩張進行中 CR 掛 @changed")
def cr_02(model: Model) -> list[Finding]:
    out = []
    crs = {c.id: c for c in model.crs}
    for spec in model.specs:
        for f in spec.features:
            deprecated = [sc for sc in f.scenarios if "@deprecated" in sc.tags]
            for d in deprecated:
                changers = [sc for sc in f.scenarios if "@changed" in sc.tags and sc.uc == d.uc]
                active = sorted({c for sc in changers for c in sc.crs if c in crs and crs[c].in_progress})
                if len(active) > 1:
                    out.append(finding(d.loc, "error", "CR-02", f'Scenario "{d.name}" 同時被進行中的 {active[0]} 與 {active[1]} 掛 @changed'))
    return out


@check("CR-03", "cr-check", "error", "CR.md 總表格式")
def cr_03(model: Model) -> list[Finding]:
    out = []
    path = model.cr_path or ".dev/CR.md"
    if not model.cr_loaded:
        out.append(finding(Loc(path, 1), "error", "CR-03", "CR.md 格式錯誤：找不到 .dev/CR.md"))
        return out
    for line, detail in model.cr_format_errors:
        out.append(finding(Loc(path, line), "error", "CR-03", f"CR.md 格式錯誤：{detail}"))
    for c in model.crs:
        for e in c.format_errors:
            out.append(finding(c.loc, "error", "CR-03", f"CR.md 格式錯誤：{e}"))
    return out


@check("CR-04", "cr-check", "error", "進行中 CR 的影響 ID 非空且存在")
def cr_04(model: Model) -> list[Finding]:
    out = []
    known = set(model.entities) | set(model.usecases) | set(model.screens)
    for c in model.crs:
        if c.status not in ("修改規格", "待處理", "處理完成"):
            continue
        if not c.impact:
            out.append(finding(c.loc, "error", "CR-04", f"{c.id} 狀態為 {c.status} 但影響 ID 為空"))
            continue
        for i in c.impact:
            if i.action != "移除" and i.id not in known:
                out.append(finding(c.loc, "error", "CR-04", f"{c.id} 狀態為 {c.status} 但影響 ID 含不存在的 {i.id}"))
    return out


@check("CR-05", "cr-check", "error", "diff 新出現的 @CR- 狀態為修改規格")
def cr_05(model: Model) -> list[Finding]:
    if model.diff is None:
        return []
    out = []
    crs = {c.id: c for c in model.crs}
    ctx: DiffContext = model.diff
    base_crs = {c for sc in ctx.base.scenarios for c in sc.crs}
    for spec in model.specs:
        for sc in spec.scenarios:
            for cid in sc.crs:
                if cid in base_crs:
                    continue
                c = crs.get(cid)
                status = c.status if c else "未登記"
                if status != "修改規格":
                    out.append(finding(sc.tag_loc or sc.loc, "error", "CR-05", f"@{cid} 出現在 diff 但總表狀態為 {status}"))
    return out


def gh_05_diff(model: Model) -> list[Finding]:
    """GH-05 的 diff 部分：模組正在開發中（衍生狀態，見 `Model.module_in_active_development`）時，

    被 diff 動到的 Scenario 必有 @CR-。「開發中」不是看檔頭，是看該模組是否有 CR 狀態為「待處理」
    （`cr-convention.md`「開發中（衍生狀態）」一節）——沒有真的在開發的模組，格式性的改動不擋。
    """
    if model.diff is None:
        return []
    out = []
    ctx: DiffContext = model.diff
    for spec in model.specs:
        if not model.module_in_active_development(spec.module):
            continue
        for sc in spec.scenarios:
            start = sc.tag_loc.line if sc.tag_loc else sc.loc.line
            if _touched(ctx, spec.path, start, sc.end_line) and not sc.crs:
                out.append(finding(sc.loc, "error", "GH-05", f'Scenario "{sc.name}" 所屬模組正在開發中，diff 動到卻沒有 @CR- tag'))
    return out
